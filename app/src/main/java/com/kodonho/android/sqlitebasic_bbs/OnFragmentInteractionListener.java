package com.kodonho.android.sqlitebasic_bbs;

import java.util.ArrayList;

public interface OnFragmentInteractionListener {
    // 일반 Action 을 처리하는 함수
    void onFragmentInteraction(int actionFlag);
    // main의 목록을 가져오는 함수
    ArrayList<BbsData> getDatas();
    // 개별 레코드(글)를 가져오는 함수
    BbsData getData(int position);
}