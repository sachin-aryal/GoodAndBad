package Service;

import Model.News;
import Model.NewsStat;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Stemmer;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by iam on 7/6/16.
 */
public class PolarityCalculator extends Thread{

    private String newsType;
    private News news;
    private NewsStat newsStat;

    public PolarityCalculator(String newsType, News news, NewsStat newsStat){
        this.newsType = newsType;
        this.news = news;
        this.newsStat = newsStat;
    }

    @Override
    public void run(){

        double polarity = getPolarity(news);

        if(newsType.equalsIgnoreCase("good")){
            if(polarity>0.0){
                newsStat.noOfNews++;
                newsStat.selectedNews.put(news.getTitle(),news.getDescription());
                newsStat.newsPolarity.put(news.getTitle(),polarity);
            }
        }else{
            if(polarity<0.0){
                newsStat.noOfNews++;
                newsStat.selectedNews.put(news.getTitle(),news.getDescription());
                newsStat.newsPolarity.put(news.getTitle(),polarity);
            }
        }

    }



    public double getPolarity(News news){

        double polarity = 0.0;
        List<String> tokenizedTitle = getTokenizedWords(news.getTitle());
        List<String> tokenizedContent = getTokenizedWords(news.getDescription());
        Stemmer s = new Stemmer();

        //// TODO: 7/7/16 Calculating Polarity for News Title and Content
        double titlePolarity = calculatePolarity(tokenizedTitle);
//        System.out.println("News Title: "+news.getTitle()+" and Polarity: "+titlePolarity);

        double contentPolarity = calculatePolarity(tokenizedContent);

//        System.out.println("After Content Polarity is "+contentPolarity);

        polarity = ((titlePolarity*6)/10)+((contentPolarity*4)/10);

//        System.out.println("Final Polarity: "+polarity);

        return polarity;
    }



    public List<String> getTokenizedWords(String content){
        TokenizerFactory<Word> tf = null;
        tf = PTBTokenizer.factory();

        List<Word> tokens_words = tf.getTokenizer(new StringReader(content)).tokenize();

        List<String> tokenizationCompleted = new ArrayList<>();
        Pattern p = Pattern.compile("[^a-z]");
        tokens_words.stream()
                .filter(item-> !p.matcher(item.word()).matches())
                .collect(Collectors.toList())
                .forEach(tokens->{
                    String tokenizedWord = tokens.word().trim();
                    if(!tokenizedWord.trim().equals("")){
                        if (!DataDictionary.wordsTobeIgnored.keySet().contains(tokenizedWord)) {
                            tokenizationCompleted.add(tokenizedWord);
                        }
                    }
                });
        return tokenizationCompleted;
    }

    public double calculatePolarity(List<String> tokenizedWord){
        Stemmer s = new Stemmer();
        double polarity=0.0;
        int itemIndex = 0;
        for (String item:tokenizedWord) {
            String stemmeredWord = s.stem(item);
            if (DataDictionary.wordDictionary.get(stemmeredWord) != null) {
                double tempPolarity = DataDictionary.wordDictionary.get(stemmeredWord);
                if (itemIndex > 0) {
                    String previousWord = tokenizedWord.get(itemIndex - 1);
                    if (DataDictionary.negationWord.keySet().contains(previousWord)) {
                        if (tempPolarity < 0) {
                            tempPolarity = Math.abs(tempPolarity);
                        } else {
                            tempPolarity = tempPolarity * -1;
                        }
                    }
                }
//                System.out.println("Key Word: " + stemmeredWord + " and Polarity: " + tempPolarity);
                polarity += tempPolarity;
            }

            itemIndex++;
        }
        return polarity;
    }
}
