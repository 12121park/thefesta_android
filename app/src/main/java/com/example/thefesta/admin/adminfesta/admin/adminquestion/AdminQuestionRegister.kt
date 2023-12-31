package com.example.thefesta.admin.adminfesta.admin.adminquestion

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.thefesta.MainActivity
import com.example.thefesta.R
import com.example.thefesta.adminbottomnavi.AdminQuestion
import com.example.thefesta.databinding.FragmentAdminQuestionRegisterBinding
import com.example.thefesta.model.admin.ReplyDTO
import com.example.thefesta.model.member.MemberDTO
import com.example.thefesta.retrofit.AdminClient
import com.example.thefesta.service.IAdminService
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminQuestionRegister : Fragment() {
    private lateinit var binding: FragmentAdminQuestionRegisterBinding
    private var bid: Int = 0
    private var content: String = ""
    private var id: String = ""
    private var nickname: String? = null

    companion object {
        private const val ARG_BID = "arg_bid"

        fun newInstance(bid: Int): AdminQuestionRegister {
            val fragment = AdminQuestionRegister()
            val args = Bundle()
            args.putInt(ARG_BID, bid)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdminQuestionRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bid = arguments?.getInt(ARG_BID) ?: 0

        if (bid != 0) {
            Log.d("AdminQuestionRegister", "bid: $bid")
            binding.adminQuestionDetailRegister.text = "문의번호 : $bid"
            id = MainActivity.prefs.getString("id", "")
            getMemberNickName(id)
        }



        binding.adminRegisterBtn.isEnabled = false // 초기에 버튼 비활성화

        binding.adminQuestionRegisterContent.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                activity?.runOnUiThread {
                    var currentByteLength: Int = 0
                    if (s != null) {
                        currentByteLength = calculateByteLength(s.toString())
                    }
                    Log.d("AdminQuestionRegister", "currentByteLength: ${currentByteLength}")

                    if (currentByteLength > 300 || currentByteLength == 0) {
                        // 3000바이트 이상이거나 글자 수가 없으면 작성완료 버튼 비활성화
                        binding.adminRegisterBtn.isEnabled = false
                        if (currentByteLength == 0) {
                            // 글자 수가 없을 때 "글자를 입력해주세요" 경고창 표시
                            Toast.makeText(requireContext(), "글자를 입력해주세요", Toast.LENGTH_SHORT).show()
                        } else {
                            // 3000바이트 이상이면 "1000글자를 초과하였습니다." 경고창 표시
                            Toast.makeText(requireContext(), "150자를 초과하였습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // 3000바이트 이하이면서 글자 수가 있는 경우 작성완료 버튼 활성화
                        binding.adminRegisterBtn.isEnabled = true
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

            private fun calculateByteLength(text: String): Int {
                var byteLength = 0
                for (char in text) {
                    byteLength += if ((char.isLetterOrDigit() || char.isWhitespace()) && !char.isHangul()) {
                        Log.d("AdminQuestionRegister", "1바이트 추가")
                        1
                    } else {
                        Log.d("AdminQuestionRegister", "byteLength: ${byteLength}")
                        Log.d("AdminQuestionRegister", "3바이트 추가")
                        3
                    }
                }
                return byteLength
            }

            private fun Char.isHangul(): Boolean {
                val unicodeBlock = Character.UnicodeBlock.of(this)
                return unicodeBlock == Character.UnicodeBlock.HANGUL_SYLLABLES || unicodeBlock == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO || unicodeBlock == Character.UnicodeBlock.HANGUL_JAMO
            }
        })



        binding.adminRegisterBtn.setOnClickListener {
            content = binding.adminQuestionRegisterContent.text.toString()
            val replyDTO = ReplyDTO(
                brno = 0,
                brcontent = content,
                brregist = "",  // Replace with the appropriate value for brregist
                bredit = "",    // Replace with the appropriate value for bredit
                bid = bid,
                nickname = nickname ?: "", // Handle the null case for nickname
                id = id,
                brstatecode = "" // Replace with the appropriate value for brstatecode
            )
            adminRegisterBtnClick(replyDTO)
        }

        binding.adminCancelBtn.setOnClickListener {
            adminCancelBtnClick()
        }

    }



    //해당 user nickname 추출
    private fun getMemberNickName(id: String) {
        val retrofit = AdminClient.retrofit

        retrofit.create(IAdminService::class.java).getMemberNickName(id)
            .enqueue(object : Callback<MemberDTO> {
                override fun onResponse(call: Call<MemberDTO>, response: Response<MemberDTO>) {
                    if (response.code() == 200) {
                        val memberDTO = response.body()
                        nickname = memberDTO?.nickname.toString()

                    } else {
                        Log.d("AdminQuestionRegister", "Failed to get member nickname: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<MemberDTO>, t: Throwable) {
                    Log.d("AdminQuestionRegister", "Network request failed", t)
                }
            })
    }


    // 작성완료 버튼 클릭
    private fun adminRegisterBtnClick(replyDto: ReplyDTO) {
        replyDto.bid = bid
        replyDto.brcontent = content
        replyDto.nickname = nickname.toString()
        replyDto.id = id

        val retrofit = AdminClient.retrofit

        retrofit.create(IAdminService::class.java).postReplies(replyDto)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.code() == 200) {
                        Log.d("AdminQuestionRegister", "저장 200: ${response.body()}")
                        Toast.makeText(requireContext(), "답변등록이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        adminRegisterComplete(bid)
                        val adminQuestion = AdminQuestion.newInstance()
                        fragmentManager?.beginTransaction()
                            ?.replace(R.id.container_admin, adminQuestion)
                            ?.addToBackStack(null)
                            ?.commit()
                    } else {
                        Log.d("AdminQuestionRegister", "Failed to delete question: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.d("AdminQuestionRegister", "Network request failed", t)
                }
            })
    }

    //문의사항 완료
    private fun adminRegisterComplete(bid: Int) {
        val retrofit = AdminClient.retrofit

        retrofit.create(IAdminService::class.java).postAdminQuestionbstatecodeChange(bid)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.code() == 200) {
                        Log.d("AdminQuestionRegister", "200: ${response.body()}")
                    } else {
                        Log.d("AdminQuestionRegister", "Failed to delete question: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.d("AdminQuestionRegister", "Network request failed", t)
                }
            })
    }

    //작성취소 버튼 클릭시
    private fun adminCancelBtnClick() {
        Toast.makeText(requireContext(), "작성이 취소되었습니다.", Toast.LENGTH_SHORT).show()
        val adminQuestionFragment = AdminQuestion.newInstance()
        fragmentManager?.beginTransaction()
            ?.replace(R.id.container_admin, adminQuestionFragment)
            ?.addToBackStack(null)
            ?.commit()
    }

}