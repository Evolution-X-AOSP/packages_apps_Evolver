/*
 * Copyright (C) 2022 FlamingoOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolution.settings.fragments

import android.annotation.ColorInt
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.Spanned
import android.text.InputFilter
import android.view.HapticFeedbackConstants.KEYBOARD_PRESS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

import androidx.core.graphics.ColorUtils
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment.STYLE_NORMAL

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.android.settings.R

class ColorPickerFragment(
    defaultColor: String? = "#FFFFFF",
) : BottomSheetDialogFragment(),
    RadioGroup.OnCheckedChangeListener,
    SeekBar.OnSeekBarChangeListener {

    private lateinit var colorPreview: View
    private lateinit var colorInput: EditText
    private lateinit var seekBarOne: SeekBar
    private lateinit var seekBarTwo: SeekBar
    private lateinit var seekBarThree: SeekBar

    private var colorModel = ColorModel.RGB
    private var textInputChangedInternal = false // Internal variable to prevent loops with TextWatcher
    private var confirmListener: (String) -> Unit = {}

    @ColorInt
    private var color: Int

    init {
        color = if (defaultColor == null || defaultColor.isEmpty()) {
            Color.WHITE
        } else {
            try {
                Color.parseColor(defaultColor)
            } catch (e: IllegalArgumentException) {
                Color.WHITE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.requestedOrientation = SCREEN_ORIENTATION_PORTRAIT
        setStyle(STYLE_NORMAL, R.style.ColorPickerStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.color_picker_layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)  {
        colorPreview = view.findViewById(R.id.color_preview)
        colorInput = view.findViewById(R.id.color_input)

        colorInput.doAfterTextChanged {
            if (textInputChangedInternal) {
                // Reset it here
                textInputChangedInternal = false
                return@doAfterTextChanged
            }
            if (it?.length != 7) return@doAfterTextChanged
            color = try {
                Color.parseColor(it.toString())
            } catch (e: IllegalArgumentException) {
                Toast.makeText(
                    context,
                    R.string.invalid_color,
                    Toast.LENGTH_SHORT
                )
                Color.WHITE
            }
            updateSliders()
            updateSliderGradients(false)
            previewColor(true)
        }

        colorInput.filters = arrayOf(
            InputFilter.LengthFilter(7),
            InputFilter filter@ { source, start, end, _, dstart, dend ->
                // Deletion
                if (start == 0 && end == 0) {
                    return@filter null
                }
                if (dstart == 0) {
                    // First character has to be # and rest of them
                    // (if present) be a valid hex char
                    if (!source.startsWith("#")) {
                        return@filter ""
                    }
                    // Just a single char
                    if (dstart == dend) return@filter null
                    if (!HEX_PATTERN.matches(source.subSequence(1, end))) {
                        return@filter ""
                    }
                } else {
                    // Does not start from 0, so every char has to be valid hex
                    if (!HEX_PATTERN.matches(source)) {
                        return@filter ""
                    }
                }
                if ((end - start) == 7) { // Full hex input
                    if (!COLOR_HEX_PATTERN.matches(source)) {
                        return@filter ""
                    }
                }
                null
            }
        )

        view.findViewById<Button>(R.id.cancel_button).setOnClickListener {
            it.performHapticFeedback(KEYBOARD_PRESS)
            dialog?.dismiss()
        }

        view.findViewById<Button>(R.id.confirm_button).setOnClickListener {
            it.performHapticFeedback(KEYBOARD_PRESS)
            dialog?.dismiss()
            val colorHex = colorInput.text.toString()
            if (colorHex.isEmpty() || colorHex.length == 7) {
                confirmListener(colorHex)
            }
        }

        /*
         * Set the drawables as mutable so that they
         * do not share a constant state or else all
         * three slider gradients will look alike
         */
        seekBarOne = view.findViewById<SeekBar>(R.id.seekBar1).also {
            it.progressDrawable.mutate()
            it.setOnSeekBarChangeListener(this)
        }
        seekBarTwo = view.findViewById<SeekBar>(R.id.seekBar2).also {
            it.progressDrawable.mutate()
            it.setOnSeekBarChangeListener(this)
        }
        seekBarThree = view.findViewById<SeekBar>(R.id.seekBar3).also {
            it.progressDrawable.mutate()
            it.setOnSeekBarChangeListener(this)
        }

        // Register listener for color model change
        view.findViewById<RadioGroup>(R.id.color_model_group).also {
            it.setOnCheckedChangeListener(this)
        }

        // Update sliders and preview
        updateSliderMax()
        updateSliders()
        updateSliderGradients(true)
        previewColor(false)
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (!fromUser) return
        color = when (colorModel) {
            ColorModel.RGB -> {
                Color.rgb(
                    seekBarOne.progress,
                    seekBarTwo.progress,
                    seekBarThree.progress
                )
            }
            ColorModel.HSV -> {
                HSVToColor(
                    seekBarOne.progress.toFloat(),
                    seekBarTwo.progress / 100f,
                    seekBarThree.progress / 100f
                )
            }
            ColorModel.HSL -> {
                HSLToColor(
                    seekBarOne.progress.toFloat(),
                    seekBarTwo.progress / 100f,
                    seekBarThree.progress / 100f
                )
            }
        }
        if (colorModel != ColorModel.RGB) {
            updateSliderGradients(false)
        }
        previewColor(false)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        // Not implemented
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        // Not implemented
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        colorModel = when (checkedId) {
            R.id.rgb_button -> ColorModel.RGB
            R.id.hsv_button -> ColorModel.HSV
            R.id.hsl_button -> ColorModel.HSL
            else -> ColorModel.RGB
        }
        updateSliderMax()
        updateSliders()
        updateSliderGradients(true)
    }

    /*
     * Set a confirmation listener that will be invoked when confirm
     * button of the dialog is pressed.
     *
     * @param listener the listener to be invoked. Hex value of the
     *      color (including # prefix and RGB) will be the type parameter
     *      of the listener. Do note that the parameter can also be empty.
     */
    fun setOnConfirmListener(listener: (String) -> Unit) {
        confirmListener = listener
    }

    /**
     * Used to update sliders if color model changes or
     * user inputs a color hex. For the latter it must be called
     * only after the accent colors are updated.
     */
    private fun updateSliders() {
        when (colorModel) {
            ColorModel.RGB -> updateSliderProgressFromColor()
            ColorModel.HSV -> {
                val array = FloatArray(3)
                Color.colorToHSV(color, array)
                updateSliderProgressFromHSVorHSL(array)
            }
            ColorModel.HSL -> {
                val array = FloatArray(3)
                ColorUtils.colorToHSL(color, array)
                updateSliderProgressFromHSVorHSL(array)
            }
        }
    }

    // For updating RGB slider progress
    private fun updateSliderProgressFromColor() {
        seekBarOne.progress = Color.red(color)
        seekBarTwo.progress = Color.green(color)
        seekBarThree.progress = Color.blue(color)
    }

    // For updating HSV / HSL slider progress
    private fun updateSliderProgressFromHSVorHSL(hsvOrHSL: FloatArray) {
        seekBarOne.progress = hsvOrHSL[0].toInt()
        seekBarTwo.progress = (hsvOrHSL[1] * 100).toInt()
        seekBarThree.progress = (hsvOrHSL[2] * 100).toInt()
    }

    // For updating the slider GradientDrawable's based on ColorModel
    private fun updateSliderGradients(colorModelChanged: Boolean) {
        if (colorModel == ColorModel.RGB) {
            if (colorModelChanged) {
                updateRGBGradient(seekBarOne.progressDrawable, Color.RED)
                updateRGBGradient(seekBarTwo.progressDrawable, Color.GREEN)
                updateRGBGradient(seekBarThree.progressDrawable, Color.BLUE)
            }
        } else {
            if (colorModelChanged) {
                updateHueGradient()
            }
            updateSaturationGradient()
            if (colorModel == ColorModel.HSV) {
                updateValueGradient()
            } else {
                updateLuminanceGradient()
            }
        }
    }

    private fun updateLuminanceGradient() {
        val drawable = seekBarThree.progressDrawable as GradientDrawable
        drawable.colors = intArrayOf(
            Color.BLACK,
            HSLToColor(
                seekBarOne.progress.toFloat(),
                seekBarTwo.progress / 100f,
                0.5f
            ),
            Color.WHITE,
        )
    }

    private fun updateValueGradient() {
        val drawable = seekBarThree.progressDrawable as GradientDrawable
        drawable.colors = intArrayOf(
            Color.BLACK,
            HSVToColor(
                seekBarOne.progress.toFloat(),
                seekBarTwo.progress / 100f,
                1f
            ),
        )
    }

    private fun updateSaturationGradient() {
        val drawable = seekBarTwo.progressDrawable as GradientDrawable
        drawable.colors = intArrayOf(
            Color.WHITE,
            if (colorModel == ColorModel.HSV) {
                HSVToColor(
                    seekBarOne.progress.toFloat(),
                    1f,
                    seekBarThree.progress / 100f
                )
            } else {
                HSLToColor(
                    seekBarOne.progress.toFloat(),
                    1f,
                    seekBarThree.progress / 100f
                )
            }
        )
    }

    private fun updateHueGradient() {
        val drawable = seekBarOne.progressDrawable as GradientDrawable
        drawable.colors = hueGradientColors
    }

    private fun updateRGBGradient(progressDrawable: Drawable, color: Int) {
        val drawable = progressDrawable as GradientDrawable
        drawable.colors = intArrayOf(Color.BLACK, color)
    }

    // inputFromUser should be set to true when user has entered a hex color
    private fun previewColor(inputFromUser: Boolean) {
        colorPreview.backgroundTintList = ColorStateList.valueOf(color)
        colorInput.setTextColor(
            if (ColorUtils.calculateLuminance(color) > 0.5) {
                Color.BLACK
            } else {
                Color.WHITE
            }
        )
        textInputChangedInternal = true
        if (!inputFromUser) {
            colorInput.setText(colorToHex(color))
        }
    }

    private fun updateSliderMax() {
        val isRGB = colorModel == ColorModel.RGB
        seekBarOne.max = if (isRGB) 255 else 360
        seekBarTwo.max = if (isRGB) 255 else 100
        seekBarThree.max = if (isRGB) 255 else 100
    }

    private enum class ColorModel {
        RGB,
        HSL,
        HSV
    }

    companion object {
        private val HEX_PATTERN = Regex("[0-9a-fA-F]+")
        private val COLOR_HEX_PATTERN = Regex("^[#][0-9a-fA-F]{6}")

        private val hueGradientColors = IntArray(7) {
            HSVToColor(it * 60f, 1f, 1f)
        }

        private fun HSVToColor(
            hue: Float,
            sat: Float,
            value: Float,
        ): Int = Color.HSVToColor(floatArrayOf(hue, sat, value))

        private fun HSLToColor(
            hue: Float,
            sat: Float,
            lum: Float,
        ): Int = ColorUtils.HSLToColor(floatArrayOf(hue, sat, lum))

        private fun colorToHex(color: Int) = String.format("#%06X", (0xFFFFFF and color))
    }
}
