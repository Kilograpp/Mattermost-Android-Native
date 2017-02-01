package com.kilogramm.mattermost.tools;

import android.text.Editable;
import android.text.Html;

import org.xml.sax.XMLReader;

/**
 * Created by Evgeny on 15.09.2016.
 */
public class MattermostTagHandler implements Html.TagHandler {
    boolean first= true;
    String parent=null;
    int index=1;
    @Override
    public void handleTag(boolean opening, String tag, Editable output,
                          XMLReader xmlReader) {

        if(tag.equals("ul"))
            parent="ul";
        else if(tag.equals("ol"))
            parent="ol";
        if(tag.equals("li")){
            if(parent.equals("ul")){
                if(first){
                    output.append("\n\t•");
                    first= false;
                }else{
                    first = true;
                }
            }
            else{
                if(first){
                    output.append("\n\t"+index+". ");
                    first= false;
                    index++;
                }else{
                    first = true;
                }
            }
        }
        if(tag.equals("hr")){
            if(opening) {
                output.append("<hr>             </hr>");
            }
        }
    }
}
