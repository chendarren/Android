package com.example.guessnumber;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {
    private int[] inputRes =
            {R.id.main_input1,R.id.main_input2,R.id.main_input3,R.id.main_input4};
    private TextView[] input = new TextView[4];
    private int[] numberRes = {R.id.main_btn_0,R.id.main_btn_1,R.id.main_btn_2,
            R.id.main_btn_3,R.id.main_btn_4,R.id.main_btn_5,R.id.main_btn_6,
            R.id.main_btn_7,R.id.main_btn_8,R.id.main_btn_9};
    //在Android中，任何的View都可以做被按下的動作 onClick
    private View[] btnNumber = new View[10]; //剛在版面上宣告的是button但這邊使用的是View，因為只是要做onClick動作而已沒有其他Button的動作了。
    private LinkedList<Integer> answer = new LinkedList<>();
    private int inputPoint;     // 輸入指標位置 0 - 3
    private LinkedList<Integer> inputValue = new LinkedList<>();   // 輸入數值陣列
    private ListView listView;
    private SimpleAdapter adapter;
    private String[] from = {"order","guess","result"};
    private int[] to = {R.id.item_order,R.id.item_guess,R.id.item_result};
    private LinkedList<HashMap<String,String>> hist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initGame();
        initListView();
    }

    // 初始化畫面
    private void initView() {
        for (int i=0; i<inputRes.length; i++){
            input[i] = findViewById(inputRes[i]);
        }
        for (int i=0; i<numberRes.length; i++){
            btnNumber[i] = findViewById(numberRes[i]);
        }
    }

    // 初始化一局遊戲
    private void initGame(){
        answer = createAnswer();
        Log.d("Darren","Answer："+answer);
        clear(null);
    }

    // 初始化 ListView
    private void initListView(){
        listView = findViewById(R.id.main_listview);
        hist = new LinkedList<>();
        adapter = new SimpleAdapter(this,hist,R.layout.item_round,from,to);
        listView.setAdapter(adapter);
    }

    // 產生謎底
    private LinkedList<Integer> createAnswer(){
        LinkedList<Integer> ret = new LinkedList<>();   //準備一個LinkedList的架構，裡面只能放整數<Integer>
        HashSet<Integer> nums = new HashSet<>();        //準備一個HashSet的架構，裡面只能放整數<Integer>
        while (nums.size()<4){      //跑迴圈去產生四個數字
            nums.add((int)(Math.random()*10));  //四個整數加到nums(數字不會重複)
        }
        for (Integer i : nums){     //for-each迴圈，把nums(沒有順序性)的元素依序丟到ret(有順序性)。
            ret.add(i);     //用ret去接
        }
        Collections.shuffle(ret);   //洗牌
        return ret;
    }

    public void inputNumber(View view) {
        if (inputPoint == 4) return;    // 此時只能 send or back or clear

        // 比對輸入鍵
        for (int i=0; i<btnNumber.length; i++){
            if (view == btnNumber[i]){
                // 輸入 i 鍵
                inputValue.set(inputPoint,i);
                input[inputPoint].setText("" + i);
                inputPoint++;
                btnNumber[i].setEnabled(false); //已按過的就不能按
                break;
            }
        }
    }

    public void back(View view) {
        if (inputPoint == 0) return;

        inputPoint--;
        btnNumber[inputValue.get(inputPoint)].setEnabled(true);
        inputValue.set(inputPoint, -1);
        input[inputPoint].setText("");
    }

    public void clear(View view) {
        inputPoint = 0;
        inputValue.clear();
        for (int i=0; i<4; i++){
            inputValue.add(-1);
        }
        for (int i = 0; i < input.length; i++) {
            input[i].setText("");
        }
        for(int i = 0; i<btnNumber.length; i++){
            btnNumber[i].setEnabled(true);      //每個數字都可以按
        }
    }

    //送出要做1.比對 2.紀錄在ListView
    public void send(View view) {
        if (inputPoint != 4) return;

        int a, b; a = b = 0; String guess = "";
        for (int i=0; i<inputValue.size(); i++){
            guess += inputValue.get(i);
            if (inputValue.get(i).equals(answer.get(i))){   //比對位置有沒有一樣的數字
                a++;
            }else if (answer.contains(inputValue.get(i))){  //比對有沒有包含在內
                b++;
            }
        }
        Log.d("Darren", a + "A" + b + "B");
        clear(null);

        HashMap<String,String> row = new HashMap<>();
        row.put(from[0], "" + (hist.size()+1));
        row.put(from[1], guess);
        row.put(from[2], a + "A" + b + "B");
        hist.add(row);
        adapter.notifyDataSetChanged();
        listView.smoothScrollToPosition(hist.size()-1);

        if (a == 4){
            // winner
            displayResult(true);
        }else if(hist.size() == 10){
            // loser
            displayResult(false);
        }
    }

    // 顯示輸贏結果
    private void displayResult(boolean isWinner){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);    //建立一個對話框的物件實體
        builder.setTitle("遊戲結果");   //給該物件一個標題

        StringBuffer ansString = new StringBuffer();
        for (int i=0; i<answer.size();i++) ansString.append(answer.get(i));

        builder.setMessage(isWinner?"完全正確":"挑戰失敗\n" + "答案:" + ansString);
        builder.setPositiveButton("開新局", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                replay(null);
            }
        });
        builder.create().show();    //使用物件builder去 創造.creates() 一個AlertDialog並且 .show()顯示 出來
    }

    public void replay(View view) {
        initGame();
        hist.clear();
        adapter.notifyDataSetChanged();
    }
}
