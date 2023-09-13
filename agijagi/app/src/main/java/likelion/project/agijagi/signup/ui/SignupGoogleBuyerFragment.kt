package likelion.project.agijagi.signup.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import likelion.project.agijagi.R
import likelion.project.agijagi.databinding.FragmentGoogleSignupBuyerBinding
import java.util.regex.Pattern

class SignupGoogleBuyerFragment : Fragment() {

    private var _fragmentGoogleSignupBuyerBinding: FragmentGoogleSignupBuyerBinding? = null
    private val fragmentGoogleSignupBuyerBinding get() = _fragmentGoogleSignupBuyerBinding!!

    private var auth: FirebaseAuth? = null
    private lateinit var db: FirebaseFirestore

    private val mAuth = auth?.currentUser

    private var nickNameState = false

    private var roleId = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragmentGoogleSignupBuyerBinding = FragmentGoogleSignupBuyerBinding.inflate(inflater, container, false)

        return fragmentGoogleSignupBuyerBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        setup()

        fragmentGoogleSignupBuyerBinding.run {
            toolbarGoogleSignupBuyerToolbar.setNavigationOnClickListener{
                findNavController().navigate(R.id.action_signupGoogleBuyerFragment_to_signupSelectFragment)
            }

            editinputGoogleSignupBuyerNickname.doAfterTextChanged { nickname ->
                nickNameState = nickname.toString().length in 2..4
                setSignupButtonState(nickNameState)
            }

            buttonGoogleSignupBuyerComplete.setOnClickListener {
                if(nickNameState == true) {
                    createUser()
                    showSnackBar("회원가입 성공했습니다.")
                }
                else {
                    showSnackBar("회원가입 실패했습니다.")
                }
            }
        }

    }

    private fun showSnackBar(message: String){
        Snackbar.make(requireView(), message, Toast.LENGTH_SHORT).show()
    }

    private fun setSignupButtonState(state: Boolean){
        if(state) {
            fragmentGoogleSignupBuyerBinding.buttonGoogleSignupBuyerComplete.isSelected = true
            fragmentGoogleSignupBuyerBinding.buttonGoogleSignupBuyerComplete.setTextColor(resources.getColor(R.color.white))
        } else {
            fragmentGoogleSignupBuyerBinding.buttonGoogleSignupBuyerComplete.isSelected = false
            fragmentGoogleSignupBuyerBinding.buttonGoogleSignupBuyerComplete.setTextColor(resources.getColor(R.color.jagi_hint_color))

        }
    }

    private fun createUser() {
        val notifSetting = hashMapOf(
            "cs_alert" to false,
            "delivery_alert" to false,
            "making_alert" to false
        )

        val buyerInfo = hashMapOf(
            "nickname" to fragmentGoogleSignupBuyerBinding.editinputGoogleSignupBuyerNickname.text.toString(),
            "notif_setting" to notifSetting
        )

        db.collection("buyer").add(buyerInfo).addOnSuccessListener {
            roleId = it.id
        }

        val userInfo = hashMapOf(
            "email" to mAuth?.email.toString(),
            "password" to "",
            "name" to mAuth?.displayName.toString(),
            "google_login_check" to true,
            "email_notif" to false,
            "sms_notif" to false,
            "is_seller" to false,
            "role_id" to roleId
        )

        db.collection("user").document(mAuth?.uid.toString())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener { Log.d("firebase", "user cloud firestore 등록 완료\n" +
                    " authUID: ${mAuth?.uid}")}
            .addOnFailureListener { e -> Log.w("firebase", "user cloud firestore 등록 실패", e)  }

        findNavController().navigate(R.id.action_signupGoogleBuyerFragment_to_loginFragment)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragmentGoogleSignupBuyerBinding = null
    }

    private fun setup() {
        db = Firebase.firestore

        val settings = firestoreSettings {
            isPersistenceEnabled = true
        }
        db.firestoreSettings = settings
    }
}