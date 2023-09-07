package likelion.project.agijagi.signup

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import likelion.project.agijagi.R
import likelion.project.agijagi.databinding.FragmentSignupBuyerBinding
import likelion.project.agijagi.databinding.FragmentSignupSellerBinding
import java.util.regex.Pattern

class SignupSellerFragment : Fragment() {

    private var _fragmentSignupSellerBinding: FragmentSignupSellerBinding? = null
    private val fragmentSignupSellerBinding get() = _fragmentSignupSellerBinding!!

    private var auth: FirebaseAuth? = null
    private lateinit var db: FirebaseFirestore

    private val emailValidation =
        "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"

    private var businessNameState = false
    private var registrationNumberState = false
    private var representativeNameState = false
    private var emailState = false
    private var passWordState = false
    private var businessAddressState = false
    private var businessNumberState = false

    private var buttonState = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _fragmentSignupSellerBinding = FragmentSignupSellerBinding.inflate(inflater, container, false)

        return fragmentSignupSellerBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        setup()

        fragmentSignupSellerBinding.run {
            toolbarSignupSellerToolbar.setNavigationOnClickListener {
                findNavController().navigate(R.id.action_signupSellerFragment_to_signupSelectFragment)
            }

            // 상호명
            editinputSignupSellerBusinessName.doAfterTextChanged { businessName ->
                businessNameState = businessName.toString().isNotBlank()
                setSignupButtonState(isValidSignupButton())
            }

            // 사업자 등록번호
            editinputSignupSellerRegistrationNumber.doAfterTextChanged { registrationNumber ->
                representativeNameState = registrationNumber.toString().isNotBlank()
                setSignupButtonState(isValidSignupButton())
            }

            // 대표자명
            editinputSignupSellerRepresentativeName.doAfterTextChanged { representativeName ->
                representativeNameState = representativeName.toString().length in 2..4
                setSignupButtonState(isValidSignupButton())
            }

            // email
            editinputSignupSellerEmail.doAfterTextChanged { email ->
                emailState = Pattern.matches(emailValidation, email)
                setSignupButtonState(isValidSignupButton())
            }

            // password
            editinputSignupSellerPassword.doAfterTextChanged { password ->
                isValiedPassWord()
                setSignupButtonState(isValidSignupButton())
            }

            // passwordCheck
            editinputSignupSellerCheckPassword.doAfterTextChanged { passwordCheck ->
                isValiedPassWord()
                setSignupButtonState(isValidSignupButton())
            }

            // 사업장 주소
            editinputSignupSellerBusinessAddress.doAfterTextChanged { businessAddress ->
                businessAddressState = businessAddress.toString().isNotBlank()
                setSignupButtonState(isValidSignupButton())
            }

            // 사업장 전화번호
            editinputSignupSellerBusinessNumber.doAfterTextChanged { businessNumber ->
                businessNumberState = businessNumber.toString().isNotBlank()
                setSignupButtonState(isValidSignupButton())
            }

            // 사업자 등록증 추가 버튼
            buttonSignupSellerAddFile.setOnClickListener {

            }

            buttonSignupSellerComplete.setOnClickListener {
                createUser(
                    email = editinputSignupSellerEmail.text.toString(),
                    password = editinputSignupSellerPassword.text.toString()
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragmentSignupSellerBinding = null
    }

    private fun isValiedPassWord(){
        fragmentSignupSellerBinding.run {
            passWordState = editinputSignupSellerPassword.text.toString() == editinputSignupSellerCheckPassword.text.toString() &&
                    editinputSignupSellerPassword.text.toString().isNotBlank() &&
                    editinputSignupSellerCheckPassword.text.toString().isNotBlank() &&
                    editinputSignupSellerPassword.text.toString().length in (6..15) &&
                    editinputSignupSellerCheckPassword.text.toString().length in (6..15)
        }
    }

    private fun isValidSignupButton(): Boolean {
        buttonState = businessNameState && registrationNumberState && representativeNameState &&
                emailState && passWordState && businessAddressState && businessNumberState
        return buttonState
    }

    private fun setSignupButtonState(state: Boolean){
        if(state) {
            fragmentSignupSellerBinding.buttonSignupSellerComplete.isSelected = true
            fragmentSignupSellerBinding.buttonSignupSellerComplete.setTextColor(resources.getColor(R.color.white))
        } else {
            fragmentSignupSellerBinding.buttonSignupSellerComplete.isSelected = false
            fragmentSignupSellerBinding.buttonSignupSellerComplete.setTextColor(resources.getColor(R.color.jagi_hint_color))

        }
    }

    private fun createUser(email: String, password: String) {
        auth?.createUserWithEmailAndPassword(email, password)
            ?.addOnCompleteListener { task ->
                // 이메일 형식 체크
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    if (task.isSuccessful) {
                        Snackbar.make(requireView(), "회원가입 성공", Toast.LENGTH_SHORT).show()
                        val user = auth?.currentUser

                        val userInfo = hashMapOf(
                            "email" to email,
                            "password" to password,
                            "name" to fragmentSignupSellerBinding.editinputSignupSellerRepresentativeName.text.toString(),
                            "google_login_check" to false,
                            "email_notif" to false,
                            "sms_notif" to false,
                            "is_seller" to true
                        )

                        db.collection("user").document(email)
                            .set(userInfo, SetOptions.merge())
                            .addOnSuccessListener { Log.d("firebase", "user cloud firestore 등록 완료\n" +
                                    " authUID: ${user?.uid}")}
                            .addOnFailureListener { e -> Log.w("firebase", "user cloud firestore 등록 실패", e)  }

                        val sellerSetting = hashMapOf(
                            "exchange" to false,
                            "inquiry" to false,
                            "order" to false
                        )

                        val sellerInfo = hashMapOf(
                            "address" to fragmentSignupSellerBinding.editinputSignupSellerBusinessAddress.text.toString(),
                            "br_cert" to "",
                            "brn" to fragmentSignupSellerBinding.editinputSignupSellerRegistrationNumber.text.toString(),
                            "bussiness_name" to fragmentSignupSellerBinding.editinputSignupSellerBusinessName.text.toString(),
                            "notif_setting" to sellerSetting,
                            "tel" to fragmentSignupSellerBinding.editinputSignupSellerBusinessNumber.text.toString()
                        )

                        db.collection("seller").document(email)
                            .set(sellerInfo, SetOptions.merge())
                            .addOnSuccessListener { Log.d("firebase", "seller cloud firestore 등록 완료\n" +
                                    " authUID: ${user?.uid}")}
                            .addOnFailureListener { e -> Log.w("firebase", "seller cloud firestore 등록 실패", e)  }

                        findNavController().navigate(R.id.action_signupSellerFragment_to_loginFragment)

                    } else {
                        Snackbar.make(requireView(), "회원가입 실패", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Snackbar.make(requireView(), "이메일 형식이 아닙니다.", Toast.LENGTH_SHORT).show()
                    fragmentSignupSellerBinding.editinputSignupSellerEmail.requestFocus()
                }
            }
    }

    private fun setup() {
        db = Firebase.firestore

        val settings = firestoreSettings {
            isPersistenceEnabled = true
        }
        db.firestoreSettings = settings
    }
}