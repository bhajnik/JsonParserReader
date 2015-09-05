package com.example.bhajnik.jsonparserreader;

import android.app.Activity;
import android.app.ListActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends Activity{
    HttpURLConnection con;
    StringBuffer sb=new StringBuffer("");
    ArrayList<person> contactList =new ArrayList<person>();
    ListView lv;
    MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String url=" http://api.androidhive.info/contacts/";
        lv=(ListView)findViewById(R.id.listView);

    }

    public void go(View v)
    {
      Thread t=new Thread(new readJson());
      t.start();
        //adapter.notifyDataSetChanged();
    }
class readJson implements Runnable
{

    String url=" http://api.androidhive.info/contacts/";

    @Override
    public void run() {
        try {
            URL url1 = new URL(url);
            con=(HttpURLConnection)url1.openConnection();
            con.setRequestMethod("GET");
            con.connect();
            BufferedReader line=new BufferedReader(new InputStreamReader(con.getInputStream()));
            String next;

            while((next=line.readLine())!=null)
            {
                //JSONArray ja =new JSONArray(next);
                Log.d("JSONString",next);
                sb.append(next);
            }
           //String json = sb.toString().substring(0, sb.toString().length()-1);
            Log.d("JSON1",sb.toString());
            JSONObject jobject=new JSONObject(sb.toString());
            JSONArray contacts=jobject.getJSONArray("contacts");
            for(int i=0;i<contacts.length();i++)
            {
                JSONObject c=contacts.getJSONObject(i);
                String id=c.getString("id");
                String name=c.getString("name");
                String email=c.getString("email");
                String address=c.getString("address");
                String gender=c.getString("gender");
                JSONObject phone = c.getJSONObject("phone");
                String mobile = phone.getString("mobile");
                String home = phone.getString("home");
                String office = phone.getString("office");
                Log.d("name", name);
                Log.d("email", email);
                Log.d("mobile", mobile);


                contactList.add(new person(name,email,mobile));

            }

                synchronized (this) {
                    wait(5000);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            adapter=new MyAdapter();
                            lv.setAdapter(adapter);
                        }
                    });

                }




        }catch (Exception e){e.printStackTrace();}
    }
}
class person
{  String name; String email;  String mobile;
    public person(String name,String email,String mobile)
    {
        this.name=name;
        this.email=email;
        this.mobile=mobile;
    }
}

class MyAdapter extends BaseAdapter
{
    @Override
    public int getCount()
    {
        return contactList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return contactList.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View singleView, ViewGroup parent)
    {
        if(singleView==null)
        {
            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            singleView = inflater.inflate(R.layout.list_items, parent, false);
        }
        TextView name1 =(TextView)singleView.findViewById(R.id.name);
        TextView email1=(TextView)singleView.findViewById(R.id.email);
        TextView mobile1=(TextView)singleView.findViewById(R.id.mobile);

        person p1=contactList.get(position);
        name1.setText(p1.name);
        email1.setText(p1.email);
        mobile1.setText(p1.mobile);

        return singleView;
}
}
}
