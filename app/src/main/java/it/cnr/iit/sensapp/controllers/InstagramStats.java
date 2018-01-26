package it.cnr.iit.sensapp.controllers;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by mattia on 26/01/18.
 */

public class InstagramStats {

    public static final String MEDIA_IMAGE = "image";
    public static final String MEDIA_VIDEO = "video";
    public static final String MEDIA_CAROUSEL = "carousel";

    public static final String[] MEDIA_TYPES = {MEDIA_IMAGE, MEDIA_VIDEO, MEDIA_CAROUSEL};

    public static List<TagFrequency> getMostUsedTags(List<String> tags, int firstNElements){

        Set<String> uniqueTags = new HashSet<>(tags);

        List<TagFrequency> tagsFrequency = new ArrayList<>();

        for(String tag : uniqueTags)
            tagsFrequency.add(new TagFrequency(tag, Collections.frequency(tags, tag)));

        Collections.sort(tagsFrequency);
        Collections.reverse(tagsFrequency);

        return tagsFrequency.subList(0,
                firstNElements > tagsFrequency.size() ? tagsFrequency.size() : firstNElements);
    }

    public static HashMap<String, Integer> getMostPublishedElementTypes(List<String> types){

        HashMap<String, Integer> frequencies = new HashMap<>();

        for(String type : MEDIA_TYPES)
            frequencies.put(type, Collections.frequency(types, type));

        return frequencies;
    }

    public static class TagFrequency implements Comparable<TagFrequency>{
        public String tag;
        public Integer frequency;

        TagFrequency(String tag, Integer frequency){
            this.tag = "#"+tag;
            this.frequency = frequency;
        }

        @Override
        public int compareTo(@NonNull TagFrequency o) {
            return this.frequency.compareTo(o.frequency);
        }
    }
}
