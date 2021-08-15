package com.dicoding.academies.ui.bookmark


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.academies.R
import com.dicoding.academies.data.source.local.entity.CourseEntity
import com.dicoding.academies.databinding.FragmentBookmarkBinding
import com.dicoding.academies.viewmodel.ViewModelFactory
import com.google.android.material.snackbar.Snackbar


/**
 * A simple [Fragment] subclass.
 */
class BookmarkFragment : Fragment(), BookmarkFragmentCallback {

    lateinit var fragmentBookmarkBinding: FragmentBookmarkBinding
//    private var viewModel: BookmarkViewModel? = null
//    private var adapter: BookmarkAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        fragmentBookmarkBinding = FragmentBookmarkBinding.inflate(inflater, container, false)
        return fragmentBookmarkBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        itemTouchHelper.attachToRecyclerView(fragmentBookmarkBinding.rvBookmark)

        if (activity != null) {
            val factory = ViewModelFactory.getInstance(requireActivity())
            val viewModel = ViewModelProvider(this, factory)[BookmarkViewModel::class.java] // using lateinit is not work

            val adapter = BookmarkAdapter(this) // using lateinit is not work

            fragmentBookmarkBinding.progressBar.visibility = View.VISIBLE
            viewModel.getBookmarks().observe(this, { courses ->
                fragmentBookmarkBinding.progressBar.visibility = View.GONE
                adapter.submitList(courses)
            })

            with(fragmentBookmarkBinding.rvBookmark) {
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
                this.adapter = adapter
            }
        }
    }

    override fun onShareClick(course: CourseEntity) {
        if (activity != null) {
            val mimeType = "text/plain"
            ShareCompat.IntentBuilder.from(requireActivity()).apply {
                setType(mimeType)
                setChooserTitle("Bagikan aplikasi ini sekarang.")
                setText("Segera daftar kelas ${course.title} di dicoding.com")
            }.startChooser()
        }
    }

//    private val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
//        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int =
//                makeMovementFlags(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
//
//        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = true
//        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//            if (view != null) {
//                val swipedPosition = viewHolder.adapterPosition
//                val courseEntity = adapter?.getSwipedData(swipedPosition)
//                courseEntity?.let { viewModel?.setBookmark(it) }
//                val snackBar = Snackbar.make(view as View, R.string.message_undo, Snackbar.LENGTH_LONG)
//                snackBar.setAction(R.string.message_ok) { _ ->
//                    courseEntity?.let { viewModel?.setBookmark(it) }
//                }
//                snackBar.show()
//            }
//        }
//    })
}