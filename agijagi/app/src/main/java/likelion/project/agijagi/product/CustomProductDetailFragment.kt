package likelion.project.agijagi.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import likelion.project.agijagi.R
import likelion.project.agijagi.databinding.FragmentCustomProductDetailBinding

class CustomProductDetailFragment : Fragment() {

    private var binding: FragmentCustomProductDetailBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomProductDetailBinding.inflate(inflater)

        clickFloatingButton()
        clickFloorPlanDownloadButton()
        clickFavoriteButton()
        clickPurchaseButtonToCustomOption()

        return binding?.root
    }

    private fun clickFloatingButton() {
        binding?.run {
            customFloatingButtonCustomProductDetailToChatting.customFloatingButtonLayout.setOnClickListener {
                Toast.makeText(context, "Custom Floating Button Clicked!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun clickFloorPlanDownloadButton() {
        binding?.run {
            buttonCustomProductDetailDownloadFloorPlan.setOnClickListener {
                Snackbar.make(it, "도면 다운로드가 완료되었습니다.", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun clickFavoriteButton() {
        binding?.run {
            imageButtonCustomProductDetailFavorite.run {
                setOnClickListener {
                    isSelected = isSelected != true
                }
            }
        }
    }

    private fun clickPurchaseButtonToCustomOption() {
        binding?.run {
            buttonCustomProductDetailPurchase.setOnClickListener {
                it.findNavController().navigate(R.id.action_customProductDetailFragment_to_customOptionFragment)
            }
        }
    }

}