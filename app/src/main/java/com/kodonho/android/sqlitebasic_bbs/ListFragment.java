package com.kodonho.android.sqlitebasic_bbs;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    ListView listView;
    Button btnWrite;

    // 메인액티비티와 통신하는 리스너
    OnFragmentListener listener;

    CustomAdapter adapter;

    // 목록에서 사용할 데이터셋 정의
    ArrayList<BbsData> datas = new ArrayList<>();

    public ListFragment() {
        // Required empty public constructor
    }

    public static ListFragment newInstance(String param1, String param2) {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        // Write 버튼
        btnWrite = (Button) view.findViewById(R.id.btnWrite);
        btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.action(MainActivity.ACTION_WRITE);
            }
        });

        listView = (ListView) view.findViewById(R.id.listView);
        adapter = new CustomAdapter(inflater);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 현재 리스트에 있는 클릭한 데이터를 가져오고
                BbsData data = datas.get(position);
                // 해당 데이터의 bbs no를 리스너를 통해 Edit Fragment로 넘겨준다
                int bbsno = data.no;
                listener.actionEdit(bbsno);
            }
        });
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // 메인 액티비티가 OnFragmentListener를 구현했는지 확인
        if(context instanceof OnFragmentListener){
            listener = (OnFragmentListener) context;
        }else{ // 구현하지 않았으면 MainActivity와 통신할 방법이 없으므로 앱을 죽인다
            throw  new RuntimeException();
        }

        setList();
    }

    public void setList(){
        datas = DataUtil.selectAll(getContext());
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    class CustomAdapter extends BaseAdapter{
        LayoutInflater inflater;
        public CustomAdapter(LayoutInflater inflater){ this.inflater = inflater; }
        @Override
        public int getCount() {
            return datas.size();
        }
        @Override
        public Object getItem(int position) {
            return datas.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) convertView = inflater.inflate(R.layout.fragment_list_item,null);
            TextView no = (TextView) convertView.findViewById(R.id.txtNo);
            TextView title = (TextView) convertView.findViewById(R.id.txtTitle);
            no.setText(datas.get(position).no+"");
            title.setText(datas.get(position).title);
            return convertView;
        }
    }
}
