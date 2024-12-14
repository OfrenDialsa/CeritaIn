package com.dicoding.picodiploma.loginwithanimation.view.screen.customview

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class PasswordEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatEditText(context, attrs, defStyleAttr) {

    init {
        setupTextWatcher()
    }

    private fun setupTextWatcher() {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().length < 8) {
                    setError("Password tidak boleh kurang dari 8 karakter", null)
                } else {
                    error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
}