package edu.bu.cs683.myflickr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import edu.bu.cs683.myflickr.databinding.FragmentAuthErrorBinding

/**
 * A simple [Fragment] subclass.
 * Use the [AuthErrorFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AuthErrorFragment : Fragment() {
    private var _binding: FragmentAuthErrorBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAuthErrorBinding.inflate(inflater, container, false)

        binding.textErrorString.text = "Unable to authenticate with Flickr due to invalid API keys."

        binding.exitApplicationButton.setOnClickListener {
            activity?.finish()
        }

        return binding.root
    }
}
