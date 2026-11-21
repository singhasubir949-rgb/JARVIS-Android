package com.example.jarvis;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {
    private static final int SPEECH = 42;
    private TextToSpeech tts;
    private TextView status, transcript, orb;
    private EditText command;
    private SharedPreferences memory;
    private boolean ready = false;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        memory = getSharedPreferences("jarvis_memory", MODE_PRIVATE);
        tts = new TextToSpeech(this, this);
        buildUi();
        if (android.os.Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 7);
    }
    private void buildUi() {
        LinearLayout root = new LinearLayout(this); root.setOrientation(1); root.setGravity(Gravity.CENTER_HORIZONTAL); root.setPadding(24,36,24,24); root.setBackgroundColor(0xFF07111F);
        TextView title = new TextView(this); title.setText("J.A.R.V.I.S"); title.setTextColor(0xFF69CFFF); title.setTextSize(30); title.setGravity(Gravity.CENTER); title.setLetterSpacing(.18f); root.addView(title);
        TextView sub = new TextView(this); sub.setText("PERSONAL ANDROID ASSISTANT"); sub.setTextColor(0xFF829CB4); sub.setTextSize(11); sub.setGravity(Gravity.CENTER); root.addView(sub);
        orb = new TextView(this); orb.setText("◉"); orb.setTextColor(0xFF38BFFF); orb.setTextSize(104); orb.setGravity(Gravity.CENTER); orb.setPadding(0,40,0,20); root.addView(orb);
        status = new TextView(this); status.setText("SYSTEM READY"); status.setTextColor(0xFF69CFFF); status.setTextSize(14); status.setGravity(Gravity.CENTER); root.addView(status);
        transcript = new TextView(this); transcript.setText("Good day. I am ready to assist you."); transcript.setTextColor(0xFFE4F3FF); transcript.setTextSize(16); transcript.setGravity(Gravity.CENTER); transcript.setPadding(12,28,12,28); root.addView(transcript);
        Button listen = button("TALK TO JARVIS"); listen.setOnClickListener(v -> listen()); root.addView(listen);
        command = new EditText(this); command.setSingleLine(true); command.setHint("Type a command..."); command.setHintTextColor(0xFF829CB4); command.setTextColor(-1); root.addView(command);
        Button send = button("EXECUTE COMMAND"); send.setOnClickListener(v -> { String s=command.getText().toString(); command.setText(""); handle(s); }); root.addView(send);
        Button mem = button("VIEW MEMORY"); mem.setOnClickListener(v -> { String m=memory.getString("facts", "No saved memories yet."); speak("Here is what I remember. " + m); }); root.addView(mem);
        Button clear = button("CLEAR MEMORY"); clear.setOnClickListener(v -> { memory.edit().remove("facts").apply(); speak("Local memory cleared."); }); root.addView(clear);
        setContentView(root);
    }
    private Button button(String label) { Button b=new Button(this); b.setText(label); b.setTextColor(0xFF07111F); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF69CFFF)); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,8,0,8); b.setLayoutParams(p); return b; }
    @Override public void onInit(int code) { if(code==TextToSpeech.SUCCESS){ tts.setLanguage(Locale.getDefault()); tts.setSpeechRate(.92f); tts.setPitch(.88f); ready=true; } }
    private void listen() {
        if (android.os.Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED) { requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},7); return; }
        Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to JARVIS"); i.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,true);
        try { status.setText("LISTENING..."); orb.setText("◉"); startActivityForResult(i,SPEECH); } catch(Exception e) { status.setText("Speech recognition unavailable"); speak("Speech recognition is not available on this device."); }
    }
    @Override protected void onActivityResult(int r,int c,Intent d){ super.onActivityResult(r,c,d); if(r==SPEECH && c==RESULT_OK && d!=null){ ArrayList<String> a=d.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS); if(a!=null&&!a.isEmpty()) handle(a.get(0)); } else status.setText("SYSTEM READY"); }
    private void handle(String raw) {
        if(raw==null||raw.trim().isEmpty()) return; String s=raw.toLowerCase(Locale.ROOT).trim(); transcript.setText("You: "+raw); status.setText("PROCESSING...");
        if(s.contains("what time")||s.equals("time")){ speak("The time is "+new SimpleDateFormat("h:mm a",Locale.getDefault()).format(new Date())); }
        else if(s.contains("date")||s.contains("today's date")){ speak("Today is "+new SimpleDateFormat("EEEE, d MMMM yyyy",Locale.getDefault()).format(new Date())); }
        else if(s.contains("battery")){ android.content.IntentFilter f=new android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED); android.content.Intent b=registerReceiver(null,f); int level=b==null? -1:b.getIntExtra("level",-1); speak(level<0?"I cannot read the battery level.":"Battery level is "+level+" percent."); }
        else if(s.startsWith("open youtube")||s.startsWith("search youtube")||s.startsWith("play ")){ String q=raw.replaceFirst("(?i)^(open youtube and )?search( youtube)?( for)? ","").replaceFirst("(?i)^play ",""); if(q.equalsIgnoreCase(raw)) q=raw.replaceFirst("(?i)^open youtube", "").trim(); openUrl("https://www.youtube.com/results?search_query="+Uri.encode(q)); speak("Here are the YouTube search results for "+q); }
        else if(s.contains("google")||s.startsWith("search for ")){ String q=raw.replaceFirst("(?i)^(search for |search google for )",""); openUrl("https://www.google.com/search?q="+Uri.encode(q)); speak("Searching the web for "+q); }
        else if(s.startsWith("remember that ")||s.startsWith("remember ")){ String fact=raw.replaceFirst("(?i)^remember( that)? ","").trim(); String old=memory.getString("facts",""); memory.edit().putString("facts", old.isEmpty()?fact:old+"; "+fact).apply(); speak("I will remember that on this device."); }
        else if(s.contains("what do you remember")||s.contains("your memory")){ String m=memory.getString("facts",""); speak(m.isEmpty()?"I do not have any saved memories yet.":"I remember: "+m); }
        else if(s.contains("flashlight")||s.contains("torch")){ speak("Flashlight control is not enabled in this starter build. I can open your phone settings instead."); startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS)); }
        else if(s.contains("volume up")){ android.media.AudioManager am=(android.media.AudioManager)getSystemService(AUDIO_SERVICE); am.adjustVolume(android.media.AudioManager.ADJUST_RAISE,android.media.AudioManager.FLAG_SHOW_UI); speak("Volume increased."); }
        else if(s.contains("volume down")){ android.media.AudioManager am=(android.media.AudioManager)getSystemService(AUDIO_SERVICE); am.adjustVolume(android.media.AudioManager.ADJUST_LOWER,android.media.AudioManager.FLAG_SHOW_UI); speak("Volume decreased."); }
        else if(s.contains("sleep jarvis")||s.contains("stop listening")){ speak("Standing by. Call me when you need me."); }
        else { speak("I can handle basic offline commands, search YouTube and Google, and remember notes. Online AI chat needs an API connection, which is not configured in this starter build."); }
    }
    private void openUrl(String url){ try { startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url))); } catch(Exception e){ speak("I could not open that link."); } }
    private void speak(String msg){ transcript.setText("JARVIS: "+msg); status.setText("SPEAKING"); orb.setText("◉"); if(ready) tts.speak(msg,TextToSpeech.QUEUE_FLUSH,null,"jarvis"); status.postDelayed(()->status.setText("SYSTEM READY"),1800); }
    @Override protected void onDestroy(){ if(tts!=null){tts.stop();tts.shutdown();} super.onDestroy(); }
}
