package com.kilogramm.mattermost.tools;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import com.yydcdut.rxmarkdown.RxMDConfiguration;
import com.yydcdut.rxmarkdown.callback.OnLinkClickCallback;
import com.yydcdut.rxmarkdown.loader.DefaultLoader;

/**
 * Created by Evgeny on 13.09.2016.
 */
public class MarkDownConfig {
    public static RxMDConfiguration getRxMDConfiguration(Context context){
        RxMDConfiguration rxMDConfiguration = new RxMDConfiguration.Builder(context)
                .setDefaultImageSize(100, 100)//default image width & height
                .setBlockQuotesColor(Color.LTGRAY)//default color of block quotes
                .setHeader1RelativeSize(2.5f)//default relative size of header1
                .setHeader2RelativeSize(2.3f)//default relative size of header2
                .setHeader3RelativeSize(2.1f)//default relative size of header3
                .setHeader4RelativeSize(1.9f)//default relative size of header4
                .setHeader5RelativeSize(1.7f)//default relative size of header5
                .setHeader6RelativeSize(1.5f)//default relative size of header6
                .setHorizontalRulesColor(Color.LTGRAY)//default color of horizontal rules's background
                .setInlineCodeBgColor(Color.LTGRAY)//default color of inline code's background
                .setCodeBgColor(Color.LTGRAY)//default color of code's background
                .setTodoColor(Color.DKGRAY)//default color of todo
                .setTodoDoneColor(Color.DKGRAY)//default color of done
                .setUnOrderListColor(Color.BLACK)//default color of unorder list
                .setLinkColor(Color.RED)//default color of link text
                .setLinkUnderline(true)//default value of whether displays link underline
                .setRxMDImageLoader(new DefaultLoader(context))//default image loader
                .setDebug(true)//default value of debug
                .setOnLinkClickCallback(new OnLinkClickCallback() {//link click callback
                    @Override
                    public void onLinkClicked(View view, String link) {
                    }
                })
                .build();
        return rxMDConfiguration;
    }
}
