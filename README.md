# 详细分析报告 Detailed Analysis Report

English version see [Report](https://github.com/davincifans101/pinduoduo_backdoor_detailed_report/blob/main/report_en.pdf), 中文分析报告见[分析报告](https://github.com/davincifans101/pinduoduo_backdoor_detailed_report/blob/main/report_cn.pdf)

# Pinduoduo恶意代码样本和脱壳机

听说PDD今天开始发律师函删帖抵赖了![PDDNB](https://user-images.githubusercontent.com/25000885/224233765-5195f16a-f41c-482f-a664-1cf72796651e.png)，那就放点新东西出来。

拼多多的两个壳，manwe和nvwa脱壳脚本。适用于样本中.mw1 .nw0文件。.nw0要用nvwa脱壳脚本，.mw1用manwe脚本。

## 拼多多manwe一键脱壳脚本

代码在`manwe_unpacker`目录，用法如下，或自己改路径：

/tmp/mw1.bin放解压出来的文件，在`/tmp/final_java/`会生成脱壳后的java class文件，压缩一下拖到jadx里看。

```java
public class ManweVmpLoader {
    public static void main(String[] args) throws Throwable {
        String firmwarePath = "/tmp/mw1.bin";
        ManweVmpDataInputStream inputStream = new ManweVmpDataInputStream(Files.newInputStream(Paths.get(firmwarePath)));
        ManweVmpDex manweVmpDex = new ManweVmpDex(inputStream);
        System.out.printf("Load %d class%n", manweVmpDex.manweVmpClazzes.length);
        if (inputStream.available() != 0) {
            throw new RuntimeException(String.format("%d bytes remaining", inputStream.available()));
        }
        inputStream.close();
        if (Files.notExists(Paths.get("/tmp/final_java/"))) {
            new File("/tmp/final_java/").mkdirs();
        }
        manweVmpDex.writeClazzes("/tmp/final_java/");
    }
}
```
## 拼多多nvwa一键脱壳脚本

代码见`nvwa_unpacker`目录

## 提取出的恶意样本

PDD的恶意代码以加壳后的文件形式组织，APK自带AliveBaseAbility，其他的都是远程下发，以下称为“样本”。因为有些样本是动态下发，不一定全，如果有这里没有的，欢迎Pull Request补充。

样本在samples目录中，包含PDD APK自带的样本，以及其动态下发的样本。动态样本为3.2日之前从安装了PDD的手机里/data/data/com.xunmeng.pinduoduo/files/bot/, /data/data/com.xunmeng.pinduoduo/files/.components/提取出，现在新版本可能被PDD删掉了，有兴趣的可以找下装了之前的版本的手机看下，顺便看下`app_mango`目录，里面是配置文件，有惊喜。

带符号的样本为PDD 6.2.0提取出(`samples/old_alive_base_ability_with_symbol/mw1.bin`)，新版本的APP携带的样本去掉了符号。

样本各个都是干货，值得看看。AliveBaseAbility是第一步，davinci仓库中提到的dex只是这个evil plan的第三步，这里其他的是第二步。

## 其他

一视同仁，平等对待才是好的营商环境，纵容、包庇不是。
据说PDD搞这个的100多号人的团队连夜解散了，删库跑路，是吗？又听说PDD这些漏洞手段被曝光停了之后，DAU出现明显下跌，是吗？
等下，有人敲门说查水表了，我先出

## 免责声明

仅用于研究用途，禁止和PDD一样作恶，没靠山别学
