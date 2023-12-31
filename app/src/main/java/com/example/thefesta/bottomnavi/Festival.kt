package com.example.thefesta.bottomnavi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.thefesta.R
import com.example.thefesta.festival.FestivalList
import com.example.thefesta.food.FoodList

class Festival : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_festival, container, false)

        navigateToFestivalListFragment()
        return view
    }

    private fun navigateToFestivalListFragment() {
        // FestivalList 프래그먼트의 인스턴스 생성
        val festivalListFragment = FestivalList()
        val transaction: FragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()

        // 현재 프래그먼트를 FestivalList 프래그먼트로 교체
        transaction.replace(R.id.container, festivalListFragment)

        // 트랜잭션을 백 스택에 추가 (선택 사항)
        transaction.addToBackStack(null)

        // 트랜잭션 커밋
        transaction.commit()
    }
}