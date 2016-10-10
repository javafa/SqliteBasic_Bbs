package com.kodonho.android.sqlitebasic_bbs;

import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnFragmentInteractionListener{
    public final static int ACTION_WRITE = 0;
    public final static int ACTION_SAVE = 1;
    public final static int ACTION_LIST = 2;
    public final static int ACTION_MODIFY = 3;

    private static ArrayList<BbsData> datas = new ArrayList<>();

    ListFragment lf; // 목록 프래그먼트
    EditFragment ef; // 쓰기 프래그먼트

    ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        // list를 호출하면서 db에서 select해온 data를 같이 넘겨준다
        lf = new ListFragment();
        ef = new EditFragment();

        pager = (ViewPager) findViewById(R.id.pager);
        CustomAdapter adapter = new CustomAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
    }

    // 앱 데이터 초기화
    private void init(){
        File file = new File(getFullpath("sqlite.db"));
        if(!file.exists())
            assetToDisk("sqlite.db");
    }

    // 프래그먼트에서 호출하는 actionFlag 의 값에 따라 동작을 결정한다
    // actionFlag 값은 상단에 final로 정의되어 있다
    @Override
    public void onFragmentInteraction(int actionFlag) {
        switch(actionFlag){
            case ACTION_WRITE:
                pager.setCurrentItem(1); // Edit 프래그먼트로 이동한다
                break;
            case ACTION_MODIFY:
                // Edit 프래그먼트로 이동하기 전에 값을 세팅한다
                pager.setCurrentItem(1);
                break;
            case ACTION_SAVE:
                pager.setCurrentItem(0); // List 프래그먼트로 이동한다
                break;
            case ACTION_LIST:
                pager.setCurrentItem(0); // List 프래그먼트로 이동한다
                break;
        }
    }

    @Override
    public ArrayList<BbsData> getDatas() {
        datas = onSelectAll();
        return datas;
    }

    @Override
    public BbsData getData(int position) {
        return datas.get(position);
    }

    /*
      여기서 부터 Database 함수
    */
    // 입력
    public void onInsert(BbsData data) {
        SQLiteDatabase db = null;
        try {
            db = openDatabase();
            if (db != null) {
                // 쿼리를 실행해준다. select 문을 제외한 모든 쿼리에 사용
                db.execSQL("insert into bbs3(name,title,contents) values('"+data.name+"','"+data.title+"','"+data.contents+"')");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if (db != null) db.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    // 수정
    public void onUpdate(BbsData data) {
        SQLiteDatabase db = null;
        try {
            db = openDatabase();
            if (db != null) {
                db.execSQL("update bbs3 " +
                        "set name='"+data.name+"' " +
                        ",title='"+data.title+"'" +
                        ",contents='"+data.contents+"' " +
                        " where no="+data.no);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if (db != null) db.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    // 모든 글 가져오기
    public ArrayList<BbsData> onSelectAll() {
        ArrayList<BbsData> datas = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = openDatabase();
            if (db != null) {
                // 기본쿼리
                String query = "select * from bbs3";
                cursor = db.rawQuery(query, null);
                while (cursor.moveToNext()) {
                    BbsData data = new BbsData();
                    int idx = cursor.getColumnIndex("no"); // 컬럼명에 해당하는 순서를 가져온다
                    data.no = cursor.getInt(idx); // 순서로 컬럼을 가져온다
                    idx = cursor.getColumnIndex("name");
                    data.name = cursor.getString(idx);
                    idx = cursor.getColumnIndex("title");
                    data.title = cursor.getString(idx);
                    idx = cursor.getColumnIndex("contents");
                    data.contents = cursor.getString(idx);
                    idx = cursor.getColumnIndex("ndate");
                    data.ndate = cursor.getString(idx);
                    datas.add(data);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if (cursor != null) cursor.close();
                if (db != null) db.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return datas;
    }

    // 데이터베이스를 연결하는 Api
    public SQLiteDatabase openDatabase() {
        return SQLiteDatabase.openDatabase(getFullpath("sqlite.db"), null, 0); // 0: 쓰기가능 1: read only
    }

    // 파일이름을 입력하면 내장 디렉토리에 있는 파일의 전체경로를 리턴해준다
    public String getFullpath(String fileName){
        // internal 디렉토리중 files 디렉토리의 경로를 가져온다
        return getFilesDir().getAbsolutePath() + File.separator + fileName;
    }

    // assets 에 있는 파일을 쓰기가능한 disk 디렉토리로 복사한다
    // 안드로이드 internal Disk 는 cache, files 등 쓰기가능한 폴더가 정해져 있다
    public void assetToDisk(String fileName){
        InputStream is = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            // 외부에서 작성된 sqlite db파일 사용하기
            // 1. assets 에 담아둔 파일을 internal 혹은 external
            //    공간으로 복사하기 위해 읽어온다
            AssetManager manager = getAssets();
            // assets 에 파일이 없으면 exception 이 발생하여 아래 로직이 실행되지 않는다
            is = manager.open(fileName);
            bis = new BufferedInputStream(is);
            // 2. 저장할 위치에 파일이 없으면 생성한다
            String targetFile = getFullpath(fileName);
            File file = new File(targetFile);
            if (!file.exists()) {
                file.createNewFile();
            }
            // 3. outputStream 을 생성해서 파일내용을 쓴다
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            // 읽어올 데이터를 담아줄 변수
            int read = -1; // 모두 읽어오면 -1이 리턴된다
            // 한번에 읽을 버퍼의 크기를 지정
            byte buffer[] = new byte[1024];
            // 더 이상 읽어올 데이터가 없을때까지 buffer 단위로 읽어서 쓴다
            while ((read = bis.read(buffer, 0, 1024)) != -1) {
                bos.write(buffer, 0, read);
            }
            // 남아 있는 데이터를 buffer에서 써준다
            bos.flush();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try {
                // 작업이 완료되면 모든 stream을 닫아준다
                if (bos != null) bos.close();
                if (fos != null) fos.close();
                if (bis != null) bis.close();
                if (is != null) is.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    /*
      여기서 부터 ViewPager용 Adapter
    */
    class CustomAdapter extends FragmentStatePagerAdapter{
        public CustomAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            // 페이저의 첫번째 화면에는 목록 프래그먼트
            //          두번째 화면에는 상세 프래그먼트를 넘겨준다
            switch (position){
                case 0: fragment=lf;break;
                case 1: fragment=ef;break;
            }
            return fragment;
        }
        @Override
        public int getCount() {
            return 2;
        }
    }
}
