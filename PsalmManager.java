package com.biblealarm.app;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PsalmManager {
    
    private static final int TOTAL_PSALMS = 150;
    private Map<Integer, String> psalmContents;
    private Map<Integer, Integer> psalmAudioResources;
    
    public PsalmManager() {
        initializePsalmContents();
        initializePsalmAudioResources();
    }
    
    /**
     * 获取今日诗篇编号（基于日期的随机算法）
     */
    public int getTodayPsalm() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        
        // 使用年份和一年中的天数作为种子，确保每天都有固定的诗篇
        Random random = new Random(year * 1000L + dayOfYear);
        return random.nextInt(TOTAL_PSALMS) + 1;
    }
    
    /**
     * 获取指定诗篇的音频资源ID
     */
    public int getPsalmAudioResource(int psalmNumber) {
        return psalmAudioResources.getOrDefault(psalmNumber, R.raw.psalm_001);
    }
    
    /**
     * 获取指定诗篇的文本内容
     */
    public String getPsalmContent(int psalmNumber) {
        return psalmContents.getOrDefault(psalmNumber, "诗篇内容加载中...");
    }
    
    /**
     * 初始化诗篇文本内容（这里只展示前几篇作为示例）
     */
    private void initializePsalmContents() {
        psalmContents = new HashMap<>();
        
        psalmContents.put(1, 
            "不从恶人的计谋，不站罪人的道路，不坐亵慢人的座位，" +
            "惟喜爱耶和华的律法，昼夜思想，这人便为有福！" +
            "他要像一棵树栽在溪水旁，按时候结果子，叶子也不枯干。" +
            "凡他所做的尽都顺利。恶人并不是这样，乃像糠秕被风吹散。" +
            "因此，当审判的时候恶人必站立不住；罪人在义人的会中也是如此。" +
            "因为耶和华知道义人的道路；恶人的道路却必灭亡。");
        
        psalmContents.put(23, 
            "耶和华是我的牧者，我必不致缺乏。" +
            "他使我躺卧在青草地上，领我在可安歇的水边。" +
            "他使我的灵魂苏醒，为自己的名引导我走义路。" +
            "我虽然行过死荫的幽谷，也不怕遭害，因为你与我同在；" +
            "你的杖，你的竿，都安慰我。" +
            "在我敌人面前，你为我摆设筵席；你用油膏了我的头，使我的福杯满溢。" +
            "我一生一世必有恩惠慈爱随着我；我且要住在耶和华的殿中，直到永远。");
        
        psalmContents.put(91, 
            "住在至高者隐密处的，必住在全能者的荫下。" +
            "我要论到耶和华说：他是我的避难所，是我的山寨，是我的神，是我所倚靠的。" +
            "他必救你脱离捕鸟人的网罗和毒害的瘟疫。" +
            "他必用自己的翎毛遮蔽你；你要投靠在他的翅膀底下；" +
            "他的诚实是大小的盾牌。你必不怕黑夜的惊骇，或是白日飞的箭，" +
            "也不怕黑暗中行的瘟疫，或是午间灭人的毒病。");
        
        psalmContents.put(121, 
            "我要向山举目；我的帮助从何而来？" +
            "我的帮助从造天地的耶和华而来。" +
            "他必不叫你的脚摇动；保护你的必不打盹！" +
            "保护以色列的，也不打盹也不睡觉。" +
            "保护你的是耶和华；耶和华在你右边荫庇你。" +
            "白日，太阳必不伤你；夜间，月亮必不害你。" +
            "耶和华要保护你，免受一切的灾害；他要保护你的性命。" +
            "你出你入，耶和华要保护你，从今时直到永远。");
        
        // 为其他诗篇添加默认内容
        for (int i = 1; i <= TOTAL_PSALMS; i++) {
            if (!psalmContents.containsKey(i)) {
                psalmContents.put(i, "诗篇第" + i + "篇\n\n愿耶和华赐福给你，保护你。" +
                    "愿耶和华使他的脸光照你，赐恩给你。" +
                    "愿耶和华向你仰脸，赐你平安。");
            }
        }
    }
    
    /**
     * 初始化诗篇音频资源映射
     */
    private void initializePsalmAudioResources() {
        psalmAudioResources = new HashMap<>();
        
        // 映射诗篇编号到音频资源
        for (int i = 1; i <= TOTAL_PSALMS; i++) {
            String resourceName = "psalm_" + String.format("%03d", i);
            int resourceId = getResourceId(resourceName);
            psalmAudioResources.put(i, resourceId != 0 ? resourceId : R.raw.default_psalm);
        }
    }
    
    /**
     * 根据资源名称获取资源ID
     */
    private int getResourceId(String resourceName) {
        try {
            return R.raw.class.getField(resourceName).getInt(null);
        } catch (Exception e) {
            return 0;
        }
    }
}