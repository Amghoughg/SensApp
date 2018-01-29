package it.cnr.iit.sensapp.utils;

import android.support.annotation.NonNull;

/**
 * Created by mattia on 27.01.18.
 */

public class ChartData {

    public static class FrequencyData implements Comparable<FrequencyData>{

        public String tag;
        public Integer frequency;

        public FrequencyData(String tag, Integer frequency, boolean withHash){
            if(withHash) this.tag = "#"+tag;
            else this.tag = tag;
            this.frequency = frequency;
        }

        @Override
        public int compareTo(@NonNull FrequencyData o) {
            return this.frequency.compareTo(o.frequency);
        }
    }
}
