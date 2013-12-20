package com.theultimatelabs.secretsanta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Application;

import com.theultimatelabs.secretsanta.NewListFragment.Participent;

public class MyApp extends Application {
	String listname;
	String password;
	Map<Integer,Map<String,String>> histories = new HashMap<Integer,Map<String,String>>();
	
	List<Participent> newListFamiles = new ArrayList<Participent>();

}
