package cn.cbsd.websockettest;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.net.URI;

public class ClientActivity extends Activity {


    URI uri = URI.create("ws://192.168.12.197:4545");

    MyWebSocketClient client = new MyWebSocketClient(uri){
        @Override
        public void onMessage(String message) {
            //message就是接收到的消息
            Log.e("serverMessage", message);
        }
    };
    TextView helloworld;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        helloworld = (TextView) findViewById(R.id.hello) ;

        try {
            client.connectBlocking();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        helloworld.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                client.send("hello");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeConnect();

    }
    private void closeConnect() {
        try {
            if (null != client) {
                client.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client = null;
        }
    }
}
