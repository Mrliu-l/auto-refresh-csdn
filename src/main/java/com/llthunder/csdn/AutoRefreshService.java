package com.llthunder.csdn;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

public class AutoRefreshService {

    private  CopyOnWriteArraySet<String> allArticleUrl = new CopyOnWriteArraySet<>();

    //博主的博客管理也地址,eg：https://blog.csdn.net/123123/
    private String url = "";

    public AutoRefreshService(String url) {
        this.url = url;
    }

    /**
     * 获取博主所有的分类专栏访问地址
     */
    public CopyOnWriteArraySet<String> getAllArticleUrl(){
        ResponseEntity<String> htmlString = new RestTemplate().getForEntity(url, String.class);
        String html = htmlString.toString();//将获取的网页转换成字符串
        //获取html元素
        Document doc = Jsoup.parse(html);
        // 获取id=asideCategory的标签,这个标签下存放的是包含分类专栏的标签，缩小范围
        Element asideCategory = doc.getElementById("asideCategory");
        //  获取id=asideCategory的标签下的ul标签
        Elements ultag = asideCategory.getElementsByTag("ul");
        // 获取id=asideCategory的标签下的ul标签的a标签===>这个标签的href存放的就是分类专栏地址
        Elements as = ultag.get(0).getElementsByTag("a");
        //遍历a标签，获取a标签中的href属性值
        as.stream().forEach(a -> {
            String href = a.attr("href");
            if (!href.isEmpty()) {
                addAllArticle_By_CategoryURL(href);
            }
        });
        System.out.println("获取url地址共有：" + allArticleUrl.size());
        return allArticleUrl;
    }

    /**
     * 根据分类专栏的地址获取分类专栏下的所有文章
     *
     * @param url
     * @return
     */
    public boolean addAllArticle_By_CategoryURL(String url) {
        ResponseEntity<String> htmlString = new RestTemplate().getForEntity(url, String.class);
        String html = htmlString.toString();//将获取的网页转换成字符串
        // 获取html元素
        Document doc = Jsoup.parse(html);
        String title = doc.getElementById("column").getElementsByClass("column_title oneline").get(0).text();//获取专栏标题

        //1、 获取类名为column_article_list的html标签，这个标签内存放的就是文章的列表
        Elements column_article_list = doc.getElementsByAttributeValue("class", "column_article_list");
        //2、 获取专栏中所有文章的li元素
        Elements li_s = column_article_list.get(0).getElementsByTag("li");
        //3、并行流处理，遍历获取文章url
        li_s.parallelStream().forEach((li) -> {
            String href = li.getElementsByTag("a").attr("href");
            if (!href.isEmpty()) {
                allArticleUrl.add(href);//存入成员变量中
            }
        });
        return true;
    }

    public void doRefresh(Long sleepSecondNum) throws InterruptedException {
        this.getAllArticleUrl();
        long i = 1;
        while (true){
            try {
                System.out.println("第" + i + "遍访问开始");
                allArticleUrl.parallelStream().forEach(articleUrl -> {
                    ResponseEntity<String> forEntity = new RestTemplate().getForEntity(articleUrl, String.class);
                    String msg = forEntity.getStatusCode() == HttpStatus.OK ? "访问成功" :  "访问失败";
                    System.out.println(articleUrl + msg);
                });
                System.out.println("第" + i + "遍访问结束");
                TimeUnit.SECONDS.sleep(sleepSecondNum);
                if(i % 10 == 0){
                    this.getAllArticleUrl();
                }
                i++;
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
    }
}
