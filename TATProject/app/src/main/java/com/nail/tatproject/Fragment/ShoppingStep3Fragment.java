package com.nail.tatproject.Fragment;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nail.tatproject.MainActivity;
import com.nail.tatproject.R;
import com.nail.tatproject.SQLite.TATDB;
import com.nail.tatproject.SQLite.TATItem;
import com.nail.tatproject.TATApplication;
import com.nail.tatproject.moudle.Product;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by 70J on 2016/6/17.
 */
public class ShoppingStep3Fragment extends Fragment {
    private LinearLayout show_product,list_product;
    private TextView shrink , final_total;
    private TextView products_sum,products_discount,products_ship,products_total,products_count;
    private final String ARG_SECTION_NUMBER = "section_number";
    private String count;
    private TATApplication Global;
    private RecyclerView listView;
    private ArrayList<Product> products = new ArrayList<>();
    private ArrayList<TATItem> IDs = new ArrayList<>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("ShoppingStep3Fragment", "onCreate");
        setRetainInstance(true);
        for (TATItem item:Global.tatdb.getAll(TATDB.Shopping_TABLE_NAME)) {
            String  id = item.getProductID();
            long  addtime = item.getAddTime();
            int  count = item.getAddCount();
            Log.d("SQLite date","id=" + id + " addtime" + addtime + " count" + count);
            IDs.add(new TATItem(id,addtime,count));
        }
        for(TATItem i:IDs){
            new AsyncGetProduct().execute("http://tatvip.ezsale.tw/tat/api/getprod.ashx", i.getProductID() ,i.getAddCount()+"" );
        }
    }

    @Override
    public void onHiddenChanged(boolean hidd) {
        if (hidd) {
            onPause();
        } else {
            onResume();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //導入Tab分頁的Fragment Layout
        Log.e("ShoppingStep3Fragment", "onCreateView");
        View view = inflater.inflate(R.layout.fragment_step3, container, false);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        show_product=(LinearLayout) view.findViewById(R.id.show_product);
        list_product=(LinearLayout) view.findViewById(R.id.list_porduct);
        shrink=(TextView) view.findViewById(R.id.shrink) ;
        final_total = (TextView)view.findViewById(R.id.final_total);
        products_sum = (TextView)view.findViewById(R.id.products_sum);
        products_discount = (TextView)view.findViewById(R.id.products_discount);
        products_ship = (TextView)view.findViewById(R.id.products_ship);
        products_total = (TextView)view.findViewById(R.id.products_total);
        products_count =  (TextView)view.findViewById(R.id.products_count);
        listView = (RecyclerView) view.findViewById(R.id.listView_product);
        SharedPreferences data = getActivity().getSharedPreferences("data",0);
        count = data.getString("products_count", null);
        int sum = Integer.valueOf(data.getString("products_sum", null));
        int discount = Integer.valueOf(data.getString("products_discount", null));
        int ship = Integer.valueOf(data.getString("products_ship", null));
        int SUM = sum- discount + discount;
        final_total.setText("$" + String.format("%,d",SUM));
        products_sum.setText("$" + sum);
        products_discount.setText("-$" + discount);
        products_ship.setText("$" + ship);
        products_total.setText("$" + String.format("%,d",SUM));
        products_count.setText("共" + count + "項商品，總計");
        list_product.setVisibility(View.GONE);
        shrink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( list_product.getVisibility()==View.VISIBLE) {
                    list_product.setVisibility(View.GONE);
                    shrink.setText("總計 " + count + " 項產品  ▽");
                }
                else {
                    list_product.setVisibility(View.VISIBLE);
                    shrink.setText("總計 " + count + " 項產品  △");
                }
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("ShoppingStep3Fragment", "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("ShoppingStep3Fragment", "onPause");
    }

    public class Items {
        Items(int newid){
            ID = newid;
        }
        int ID = 1;
    }

    class AsyncGetProduct extends AsyncTask<String, Integer, Integer> {
        //================================================================
        String Reply;
        int receive_count = 1;
        @Override
        protected Integer doInBackground(String... param) {
            //get Data 單存取資料
            Gson gson = new Gson();
            String content = null;
            try {
                content = "CheckM=" + URLEncoder.encode("286e5560eeac9d7ecb7ecbb6968148c7", "UTF-8");
                content +="&SiteID="+URLEncoder.encode("778", "UTF-8");
                content +="&Type="+URLEncoder.encode("4", "UTF-8");
                int id = Integer.valueOf(param[1]);
                receive_count = Integer.valueOf(param[2]);
                content +="&Items="+gson.toJson(new Items(id));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return 0;
            }
            Reply = makeHttpRequest(param[0],"POST",content);
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            //此method是在doInBackground完成以後，才會呼叫的
            super.onPostExecute(result);
            if(result == 1){
                Log.d("result",Reply);
                try {
                    //http://tatex.ezsale.tw/upload/1SP-OK-001(1).JPG
                    JSONObject json_data = new JSONObject(Reply);
                    com.nail.tatproject.moudle.Product module = new com.nail.tatproject.moudle.Product();
                    module.id = json_data.optInt("ID");
                    module.subid = json_data.optInt("SubID");
                    module.image_URL = json_data.optString("Img1");
                    //module.price = json_data.optInt("Value1");
                    module.price = 1500;
                    module.name = json_data.optString("Title");
                    if(!json_data.isNull("Stock")) {
                        if (json_data.getJSONObject("Stock").has("Num")) {
                            module.product_max = json_data.getJSONObject("Stock").optInt("Num");
                        }
                        else module.product_max = 10;
                    }
                    else module.product_max = 10;
                    module.type = module.id + "";
                    module.count = receive_count;
                    products.add(module);
                    Log.d("Product", "ID=" + module.id +" SubID=" + module.subid +" URL=" + module.image_URL +" price=" + module.price+" count=" + module.count  + " max=" + module.product_max);
                    /*ContactAdapter customAdapter = new ContactAdapter(products);
                    listView.setAdapter(customAdapter);*/
                    LinearLayoutManager llm = new LinearLayoutManager(getActivity());
                    llm.setAutoMeasureEnabled(true);
                    llm.setOrientation(LinearLayoutManager.VERTICAL);
                    listView.setLayoutManager(llm);
                } catch (Exception e) {
                    Log.e("JSON Parser", "Error parsing data " + e.toString());
                    e.printStackTrace();
                }
            }
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        //=============================================================
    }

    public String makeHttpRequest(String temp_url, String method, String urlParameters){
        HttpURLConnection conn = null;
        try{
            // 建立連線
            URL url =new URL(temp_url);
            conn = (HttpURLConnection)url.openConnection();
            //===============================
            conn.setDoOutput(true);
            // Read from the connection. Default is true.
            conn.setDoInput(true);
            // conn method
            conn.setRequestMethod(method);

            // Post 请求不能使用缓存
            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(true);

            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

            //Send request
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            //Get Response
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            StringBuffer sb = new StringBuffer();

            while((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            reader.close();
            return sb.toString();
        }catch (Exception e) {
            System.out.println("makeHttpRequest Error:"+e.toString());
        }finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;

        public ImageDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }
        @Override
        protected Bitmap doInBackground(String... params) {
            return downloadBitmap(params[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }
            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    } else {
                        imageView.setImageDrawable(null);
                    }
                }
            }
        }
    }
    private Bitmap downloadBitmap(String url) {
        HttpURLConnection urlConnection = null;
        try {
            URL uri = new URL(url);
            urlConnection = (HttpURLConnection) uri.openConnection();
            int statusCode = urlConnection.getResponseCode();
            if (statusCode != 200) {
                return null;
            }

            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream != null) {
                return BitmapFactory.decodeStream(inputStream);
            }
        } catch (Exception e) {
            Log.w("ImageDownloader", "Error downloading image from " + url);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }
}
