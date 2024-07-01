package com.example.seasonsapp

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment

class SeasonNoteFragment : Fragment() {

    private lateinit var season: String
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var notesContainer: LinearLayout
    private lateinit var backgroundImageView: ImageView
    private var isAddingNote = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        season = arguments?.getString("season") ?: "spring"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_season_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("seasons_prefs", Context.MODE_PRIVATE)

        val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
        backgroundImageView = view.findViewById(R.id.season_background)
        notesContainer = view.findViewById(R.id.notesContainer)

        val seasonName = getSeasonName(season)
        val titleText = getString(R.string.season_title, seasonName)

        // Apply styles to the title text
        val seasonIndex = titleText.indexOf(seasonName)
        val spannable = SpannableString(titleText).apply {
            setSpan(ForegroundColorSpan(getSeasonColor(season)), seasonIndex, seasonIndex + seasonName.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(StyleSpan(Typeface.BOLD), seasonIndex, seasonIndex + seasonName.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            val kopubMedium = ResourcesCompat.getFont(requireContext(), R.font.kopubmedium)
            if (kopubMedium != null) {
                setSpan(CustomTypefaceSpan(kopubMedium), seasonIndex, seasonIndex + seasonName.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        titleTextView.text = spannable
        titleTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

        backgroundImageView.setImageDrawable(getSeasonDrawable(season))

        loadNotes()

        view.setOnClickListener {
            if (!isAddingNote) {
                addNewNote()
            }
        }
    }

    private fun getSeasonName(season: String): String {
        return when (season) {
            "spring" -> "봄"
            "summer" -> "여름"
            "autumn" -> "가을"
            "winter" -> "겨울"
            else -> "봄"
        }
    }

    private fun getSeasonDrawable(season: String): Drawable? {
        val resId = when (season) {
            "spring" -> R.drawable.spr31
            "summer" -> R.drawable.sum31
            "autumn" -> R.drawable.aut31
            "winter" -> R.drawable.win31
            else -> R.drawable.spr31
        }
        return ResourcesCompat.getDrawable(resources, resId, null)
    }

    private fun getSeasonColor(season: String): Int {
        return when (season) {
            "spring" -> ContextCompat.getColor(requireContext(), R.color.spColor)
            "summer" -> ContextCompat.getColor(requireContext(), R.color.suColor)
            "autumn" -> ContextCompat.getColor(requireContext(), R.color.auColor)
            "winter" -> ContextCompat.getColor(requireContext(), R.color.wiColor)
            else -> ContextCompat.getColor(requireContext(), R.color.black)
        }
    }

    private fun loadNotes() {
        val notes = sharedPreferences.getStringSet("${season}_notes", emptySet()) ?: return
        notesContainer.removeAllViews()
        notes.forEach {
            val textView = TextView(context).apply {
                text = it
                setTextAppearance(R.style.NoteTextStyle)
                gravity = View.TEXT_ALIGNMENT_CENTER
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 48, 0, 48) // Add top and bottom margins
                }
                setOnClickListener { editNoteTextView(this) }
            }
            notesContainer.addView(textView)
        }
    }

    private fun addNewNote() {
        if (notesContainer.childCount >= 6) return

        isAddingNote = true

        val editText = EditText(context).apply {
            hint = "당신의 계절을 입력하세요"
            maxLines = 1
            inputType = InputType.TYPE_CLASS_TEXT
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = View.TEXT_ALIGNMENT_CENTER
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            imeOptions = EditorInfo.IME_ACTION_DONE
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val enteredText = text.toString()
                    if (enteredText.isNotEmpty()) {
                        saveNoteToPreferences(enteredText)
                        val textView = TextView(context).apply {
                            text = enteredText
                            setTextAppearance(R.style.NoteTextStyle)
                            gravity = View.TEXT_ALIGNMENT_CENTER
                            textAlignment = View.TEXT_ALIGNMENT_CENTER
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, 48, 0, 48) // Add top and bottom margins
                            }
                            setOnClickListener { editNoteTextView(this) }
                        }
                        notesContainer.addView(textView)
                    }
                    notesContainer.removeView(this)
                    isAddingNote = false
                    hideKeyboard()
                    true
                } else {
                    false
                }
            }
            setOnKeyListener { v, keyCode, event ->
                if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    val enteredText = text.toString()
                    if (enteredText.isNotEmpty()) {
                        saveNoteToPreferences(enteredText)
                        val textView = TextView(context).apply {
                            text = enteredText
                            setTextAppearance(R.style.NoteTextStyle)
                            gravity = View.TEXT_ALIGNMENT_CENTER
                            textAlignment = View.TEXT_ALIGNMENT_CENTER
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, 48, 0, 48) // Add top and bottom margins
                            }
                            setOnClickListener { editNoteTextView(this) }
                        }
                        notesContainer.addView(textView)
                    }
                    notesContainer.removeView(this)
                    isAddingNote = false
                    hideKeyboard()
                    true
                } else {
                    false
                }
            }
        }

        notesContainer.addView(editText)
        editText.requestFocus()
        showKeyboard()
    }

    private fun editNoteTextView(textView: TextView) {
        val currentText = textView.text.toString()
        val currentIndex = notesContainer.indexOfChild(textView)
        notesContainer.removeView(textView)
        val editText = EditText(context).apply {
            setText(currentText)
            maxLines = 1
            inputType = InputType.TYPE_CLASS_TEXT
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = View.TEXT_ALIGNMENT_CENTER
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            imeOptions = EditorInfo.IME_ACTION_DONE
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val enteredText = text.toString()
                    if (enteredText.isNotEmpty()) {
                        saveNoteToPreferences(enteredText)
                        val newTextView = TextView(context).apply {
                            text = enteredText
                            setTextAppearance(R.style.NoteTextStyle)
                            gravity = View.TEXT_ALIGNMENT_CENTER
                            textAlignment = View.TEXT_ALIGNMENT_CENTER
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, 48, 0, 48) // Add top and bottom margins
                            }
                            setOnClickListener { editNoteTextView(this) }
                        }
                        notesContainer.addView(newTextView, currentIndex)
                    }
                    notesContainer.removeView(this)
                    if (enteredText.isEmpty()) {
                        val notes = sharedPreferences.getStringSet("${season}_notes", mutableSetOf())?.toMutableSet()
                        notes?.remove(currentText)
                        sharedPreferences.edit().putStringSet("${season}_notes", notes).apply()
                    }
                    isAddingNote = false
                    hideKeyboard()
                    true
                } else {
                    false
                }
            }
            setOnKeyListener { v, keyCode, event ->
                if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    val enteredText = text.toString()
                    if (enteredText.isNotEmpty()) {
                        saveNoteToPreferences(enteredText)
                        val newTextView = TextView(context).apply {
                            text = enteredText
                            setTextAppearance(R.style.NoteTextStyle)
                            gravity = View.TEXT_ALIGNMENT_CENTER
                            textAlignment = View.TEXT_ALIGNMENT_CENTER
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, 48, 0, 48) // Add top and bottom margins
                            }
                            setOnClickListener { editNoteTextView(this) }
                        }
                        notesContainer.addView(newTextView, currentIndex)
                    }
                    notesContainer.removeView(this)
                    if (enteredText.isEmpty()) {
                        val notes = sharedPreferences.getStringSet("${season}_notes", mutableSetOf())?.toMutableSet()
                        notes?.remove(currentText)
                        sharedPreferences.edit().putStringSet("${season}_notes", notes).apply()
                    }
                    isAddingNote = false
                    hideKeyboard()
                    true
                } else {
                    false
                }
            }
        }

        notesContainer.addView(editText, currentIndex)
        editText.requestFocus()
        editText.setSelection(editText.text.length)
        showKeyboard()
    }

    private fun saveNoteToPreferences(note: String) {
        val notes = sharedPreferences.getStringSet("${season}_notes", mutableSetOf())?.toMutableSet()
        notes?.add(note)
        sharedPreferences.edit().putStringSet("${season}_notes", notes).apply()
    }

    private fun showKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun hideKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    fun setSeasonBackgroundAlpha(alpha: Float) {
        backgroundImageView.alpha = alpha
    }

    companion object {
        fun newInstance(season: String) = SeasonNoteFragment().apply {
            arguments = Bundle().apply {
                putString("season", season)
            }
        }
    }
}
