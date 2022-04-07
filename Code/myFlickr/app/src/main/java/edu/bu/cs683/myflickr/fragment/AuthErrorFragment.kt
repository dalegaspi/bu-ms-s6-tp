package edu.bu.cs683.myflickr.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import edu.bu.cs683.myflickr.R
import edu.bu.cs683.myflickr.databinding.FragmentAuthErrorBinding

/**
 * This fragment is used to display an error when there are issues with authentication
 * with the Flickr API
 *
 * @author dlegaspi@bu.edu
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

        binding.textErrorString.text = getString(R.string.flickr_api_auth_error)

        binding.exitApplicationButton.setOnClickListener {
            activity?.finish()
        }

        return binding.root
    }
}
