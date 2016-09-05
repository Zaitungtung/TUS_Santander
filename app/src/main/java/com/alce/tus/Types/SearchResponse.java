package com.alce.tus.Types;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SearchResponse {

    @SerializedName("resources")
    public ArrayList<Item> items;

}