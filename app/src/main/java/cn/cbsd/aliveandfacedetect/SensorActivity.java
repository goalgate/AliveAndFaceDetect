package cn.cbsd.aliveandfacedetect;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class SensorActivity extends Activity {
    private TextView tv;
    private SensorManager manager;
    private MySensorEventListener listener;
    private Sensor magneticSensor, accelerometerSensor;
    private float[] values, r, gravity, geomagnetic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        tv = (TextView) findViewById(R.id.tv);
        //获取SensorManager
        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        listener = new MySensorEventListener();
        //获取Sensor
        magneticSensor = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometerSensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //初始化数组
        values = new float[3];//用来保存最终的结果
        gravity = new float[3];//用来保存加速度传感器的值
        r = new float[9];//
        geomagnetic = new float[3];//用来保存地磁传感器的值
    }

    private class MySensorEventListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagnetic = event.values;

                Log.e("geo", "1:" + geomagnetic[0] +
                        "2:" + geomagnetic[1] +
                        "3:" + geomagnetic[2]);
            }
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {


                gravity = event.values;
                getValue();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        manager.registerListener(listener, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
        manager.registerListener(listener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        manager.unregisterListener(listener);
        super.onPause();
    }

    public void getValue() {
        // r从这里返回
        float x = gravity[SensorManager.DATA_X];
        float y = gravity[SensorManager.DATA_Y];
        float z = gravity[SensorManager.DATA_Z];
        tv.setText("x：" + (int) x + "\ny：" + (int) y + "\nz：" + (int) z);

//        SensorManager.getRotationMatrix(r, null, gravity, geomagnetic);
//        //values从这里返回
//        SensorManager.getOrientation(r, values);
//        //提取数据
//        double azimuth = Math.toDegrees(values[0]);
//        if (azimuth < 0) {
//            azimuth = azimuth + 360;
//        }
//        double pitch = Math.toDegrees(values[1]);
//        double roll = Math.toDegrees(values[2]);
//        tv.invalidate();
//        tv.setText("Azimuth：" + (int) azimuth + "\nPitch：" + (int) pitch + "\nRoll：" + (int) roll);
//
    }

}