package cn.edu.pku.lxy.myweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ImageView;

public class SelectCity extends Activity implements View.OnClickListener {
    private ImageView mBackBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_city);
        mBackBtn=(ImageView)findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);
    }
    @Override
    public void onClick(View v){
        if(v.getId()==R.id.title_back){
            Intent i=new Intent();
            i.putExtra("cityCode","101160101");
            setResult(RESULT_OK,i);
            finish();
        }
    }
}
