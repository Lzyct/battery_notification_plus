package com.tuxdev.batterynotifplus.widget

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.content.res.ResourcesCompat
import com.tuxdev.batterynotifplus.R


class WaveLoadingView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    // Dynamic Properties.
    private var mCanvasSize: Int = 0
    private var mCanvasHeight: Int = 0
    private var mCanvasWidth: Int = 0
    var amplitudeRatio: Float = 0.toFloat()
        private set
    private var mWaveBgColor: Int = 0
    private var mWaveColor: Int = 0
    var shapeType: Int = 0
        private set
    private var mTriangleDirection: Int = 0
    private var mRoundRectangleXY: Int = 0
    // Properties.
    /**
     * Set the title within the WaveView.
     *
     * @param topTitle Default to be null.
     */

    var topTitle: String? = ""
    var centerTitle: String? = ""
    var bottomTitle: String? = ""
    private var mDefaultWaterLevel: Float = 0.toFloat()
    var waterLevelRatio = 1f
        set(waterLevelRatio) {
            if (this.waterLevelRatio != waterLevelRatio) {
                field = waterLevelRatio
                invalidate()
            }
        }
    var waveShiftRatio = DEFAULT_WAVE_SHIFT_RATIO
        set(waveShiftRatio) {
            if (this.waveShiftRatio != waveShiftRatio) {
                field = waveShiftRatio
                invalidate()
            }
        }
    private var mProgressValue = DEFAULT_WAVE_PROGRESS_VALUE
    private var mIsRoundRectangle: Boolean = false
    // Object used to draw.
    // Shader containing repeated waves.
    private var mWaveShader: BitmapShader? = null
    private val bitmapBuffer: Bitmap? = null
    // Shader matrix.
    private var mShaderMatrix: Matrix? = null
    // Paint to draw wave.
    private var mWavePaint: Paint? = null
    //Paint to draw waveBackground.
    private var mWaveBgPaint: Paint? = null
    // Paint to draw border.
    private var mBorderPaint: Paint? = null
    // Point to draw title.
    private var mTopTitlePaint: Paint? = null
    private var mBottomTitlePaint: Paint? = null
    private var mCenterTitlePaint: Paint? = null
    private var mTopTitleStrokePaint: Paint? = null
    private var mBottomTitleStrokePaint: Paint? = null
    //    private var mCenterTitleStrokePaint: Paint? = null
    // Animation.
    private var waveShiftAnim: ObjectAnimator? = null
    private var mAnimatorSet: AnimatorSet? = null
    private var mContext: Context? = null

    var waveBgColor: Int
        get() = mWaveBgColor
        set(color) {
            this.mWaveBgColor = color
            mWaveBgPaint!!.color = this.mWaveBgColor
            updateWaveShader()
            invalidate()
        }

    // Need to recreate shader when color changed ?
    //        mWaveShader = null;
    var waveColor: Int
        get() = mWaveColor
        set(color) {
            mWaveColor = color
            updateWaveShader()
            invalidate()
        }

    var borderWidth: Float
        get() = mBorderPaint!!.strokeWidth
        set(width) {
            mBorderPaint!!.strokeWidth = width
            invalidate()
        }

    var borderColor: Int
        get() = mBorderPaint!!.color
        set(color) {
            mBorderPaint!!.color = color
            updateWaveShader()
            invalidate()
        }

    /**
     * Water level increases from 0 to the value of WaveView.
     *
     * @param progress Default to be 50.
     */
    var progressValue: Int
        get() = mProgressValue
        set(progress) {
            mProgressValue = progress
            val waterLevelAnim = ObjectAnimator.ofFloat(this, "waterLevelRatio", waterLevelRatio, mProgressValue.toFloat() / 100)
            waterLevelAnim.duration = 1000
            waterLevelAnim.interpolator = DecelerateInterpolator()
            val animatorSetProgress = AnimatorSet()
            animatorSetProgress.play(waterLevelAnim)
            animatorSetProgress.start()
        }

    var topTitleColor: Int
        get() = mTopTitlePaint!!.color
        set(topTitleColor) {
            mTopTitlePaint!!.color = topTitleColor
        }

    var centerTitleColor: Int
        get() = mCenterTitlePaint!!.color
        set(centerTitleColor) {
            mCenterTitlePaint!!.color = centerTitleColor
        }

    var bottomTitleColor: Int
        get() = mBottomTitlePaint!!.color
        set(bottomTitleColor) {
            mBottomTitlePaint!!.color = bottomTitleColor
        }

    var centerTitleSize: Float
        get() = mCenterTitlePaint!!.textSize
        set(centerTitleSize) {
            mCenterTitlePaint!!.textSize = sp2px(centerTitleSize).toFloat()
        }

    var bottomTitleSize: Float
        get() = mBottomTitlePaint!!.textSize
        set(bottomTitleSize) {
            mBottomTitlePaint!!.textSize = sp2px(bottomTitleSize).toFloat()
        }

    init {
        init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        //set typeface
        val face = ResourcesCompat.getFont(context, R.font.gotham_bold)

        mContext = context
        // Init Wave.
        mShaderMatrix = Matrix()
        mWavePaint = Paint()
        // The ANTI_ALIAS_FLAG bit AntiAliasing smooths out the edges of what is being drawn,
        // but is has no impact on the interior of the shape.
        mWavePaint!!.isAntiAlias = true
        mWaveBgPaint = Paint()
        mWaveBgPaint!!.isAntiAlias = true
        // Init Animation
        initAnimation()

        // Load the styled attributes and set their properties
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.WaveLoadingView, defStyleAttr, 0)

        // Init ShapeType
        shapeType = attributes.getInteger(R.styleable.WaveLoadingView_wlv_shapeType, DEFAULT_WAVE_SHAPE)

        // Init Wave
        mWaveColor = attributes.getColor(R.styleable.WaveLoadingView_wlv_waveColor, DEFAULT_WAVE_COLOR)
        mWaveBgColor = attributes.getColor(R.styleable.WaveLoadingView_wlv_wave_background_Color, DEFAULT_WAVE_BACKGROUND_COLOR)

        mWaveBgPaint!!.color = mWaveBgColor

        // Init AmplitudeRatio
        val amplitudeRatioAttr = attributes.getFloat(R.styleable.WaveLoadingView_wlv_waveAmplitude, DEFAULT_AMPLITUDE_VALUE) / 1000
        amplitudeRatio = if (amplitudeRatioAttr > DEFAULT_AMPLITUDE_RATIO) DEFAULT_AMPLITUDE_RATIO else amplitudeRatioAttr

        // Init Progress
        mProgressValue = attributes.getInteger(R.styleable.WaveLoadingView_wlv_progressValue, DEFAULT_WAVE_PROGRESS_VALUE)
        progressValue = mProgressValue

        // Init RoundRectangle
        mIsRoundRectangle = attributes.getBoolean(R.styleable.WaveLoadingView_wlv_round_rectangle, false)
        mRoundRectangleXY = attributes.getInteger(R.styleable.WaveLoadingView_wlv_round_rectangle_x_and_y, DEFAULT_ROUND_RECTANGLE_X_AND_Y)

        // Init Triangle direction
        mTriangleDirection = attributes.getInteger(R.styleable.WaveLoadingView_wlv_triangle_direction, DEFAULT_TRIANGLE_DIRECTION)

        // Init Border
        mBorderPaint = Paint()
        mBorderPaint!!.isAntiAlias = true
        mBorderPaint!!.style = Paint.Style.STROKE
        mBorderPaint!!.strokeWidth = attributes.getDimension(R.styleable.WaveLoadingView_wlv_borderWidth, dp2px(DEFAULT_BORDER_WIDTH).toFloat())
        mBorderPaint!!.color = attributes.getColor(R.styleable.WaveLoadingView_wlv_borderColor, DEFAULT_WAVE_COLOR)

        // Init Top Title
        mTopTitlePaint = Paint()
        mTopTitlePaint!!.color = attributes.getColor(R.styleable.WaveLoadingView_wlv_titleTopColor, DEFAULT_TITLE_COLOR)
        mTopTitlePaint!!.style = Paint.Style.FILL
        mTopTitlePaint!!.isAntiAlias = true
        mTopTitlePaint!!.textSize = attributes.getDimension(R.styleable.WaveLoadingView_wlv_titleTopSize, sp2px(DEFAULT_TITLE_TOP_SIZE).toFloat())
        mTopTitlePaint!!.typeface = face

        mTopTitleStrokePaint = Paint()
        mTopTitleStrokePaint!!.color = attributes.getColor(R.styleable.WaveLoadingView_wlv_titleTopStrokeColor, DEFAULT_STROKE_COLOR)
        mTopTitleStrokePaint!!.strokeWidth = attributes.getDimension(R.styleable.WaveLoadingView_wlv_titleTopStrokeWidth, dp2px(DEFAULT_TITLE_STROKE_WIDTH).toFloat())
        mTopTitleStrokePaint!!.style = Paint.Style.STROKE
        mTopTitleStrokePaint!!.isAntiAlias = true
        mTopTitleStrokePaint!!.textSize = mTopTitlePaint!!.textSize

        topTitle = attributes.getString(R.styleable.WaveLoadingView_wlv_titleTop)

        // Init Center Title
        mCenterTitlePaint = Paint()
        mCenterTitlePaint!!.color = attributes.getColor(R.styleable.WaveLoadingView_wlv_titleCenterColor, DEFAULT_TITLE_COLOR)
        mCenterTitlePaint!!.style = Paint.Style.FILL
        mCenterTitlePaint!!.isAntiAlias = true
        mCenterTitlePaint!!.textSize = attributes.getDimension(R.styleable.WaveLoadingView_wlv_titleCenterSize, sp2px(DEFAULT_TITLE_CENTER_SIZE).toFloat())
        mCenterTitlePaint!!.setShadowLayer(5F, 3F, 3F, attributes.getColor(R.styleable.WaveLoadingView_wlv_titleCenterStrokeColor, DEFAULT_STROKE_COLOR))
        mCenterTitlePaint!!.typeface = face


//        mCenterTitleStrokePaint = Paint()
//       mCenterTitleStrokePaint!!.color = attributes.getColor(R.styleable.WaveLoadingView_wlv_titleCenterStrokeColor, DEFAULT_STROKE_COLOR)
//        mCenterTitleStrokePaint!!.strokeWidth = attributes.getDimension(R.styleable.WaveLoadingView_wlv_titleCenterStrokeWidth, dp2px(DEFAULT_TITLE_STROKE_WIDTH).toFloat())
//        mCenterTitleStrokePaint!!.style = Paint.Style.STROKE
//        mCenterTitleStrokePaint!!.isAntiAlias = true
//        mCenterTitleStrokePaint!!.textSize = mCenterTitlePaint!!.textSize

        if (attributes.getString(R.styleable.WaveLoadingView_wlv_titleCenter) != null)
            centerTitle = attributes.getString(R.styleable.WaveLoadingView_wlv_titleCenter)

        // Init Bottom Title
        mBottomTitlePaint = Paint()
        mBottomTitlePaint!!.color = attributes.getColor(R.styleable.WaveLoadingView_wlv_titleBottomColor, DEFAULT_TITLE_COLOR)
        mBottomTitlePaint!!.style = Paint.Style.FILL
        mBottomTitlePaint!!.isAntiAlias = true
        mBottomTitlePaint!!.textSize = attributes.getDimension(R.styleable.WaveLoadingView_wlv_titleBottomSize, sp2px(DEFAULT_TITLE_BOTTOM_SIZE).toFloat())
        mBottomTitlePaint!!.typeface = face

        mBottomTitleStrokePaint = Paint()
        mBottomTitleStrokePaint!!.color = attributes.getColor(R.styleable.WaveLoadingView_wlv_titleBottomStrokeColor, DEFAULT_STROKE_COLOR)
        mBottomTitleStrokePaint!!.strokeWidth = attributes.getDimension(R.styleable.WaveLoadingView_wlv_titleBottomStrokeWidth, dp2px(DEFAULT_TITLE_STROKE_WIDTH).toFloat())
        mBottomTitleStrokePaint!!.style = Paint.Style.STROKE
        mBottomTitleStrokePaint!!.isAntiAlias = true
        mBottomTitleStrokePaint!!.textSize = mBottomTitlePaint!!.textSize

        bottomTitle = attributes.getString(R.styleable.WaveLoadingView_wlv_titleBottom)

        attributes.recycle()
    }

    public override fun onDraw(canvas: Canvas) {
        mCanvasSize = canvas.width
        if (canvas.height < mCanvasSize) {
            mCanvasSize = canvas.height
        }
        // Draw Wave.
        // Modify paint shader according to mShowWave state.
        if (mWaveShader != null) {
            // First call after mShowWave, assign it to our paint.
            if (mWavePaint!!.shader == null) {
                mWavePaint!!.shader = mWaveShader
            }

            // Sacle shader according to waveLengthRatio and amplitudeRatio.
            // This decides the size(waveLengthRatio for width, amplitudeRatio for height) of waves.
            mShaderMatrix!!.setScale(1f, amplitudeRatio / DEFAULT_AMPLITUDE_RATIO, 0f, mDefaultWaterLevel)
            // Translate shader according to waveShiftRatio and waterLevelRatio.
            // This decides the start position(waveShiftRatio for x, waterLevelRatio for y) of waves.
            mShaderMatrix!!.postTranslate(waveShiftRatio * width,
                    (DEFAULT_WATER_LEVEL_RATIO - waterLevelRatio) * height)

            // Assign matrix to invalidate the shader.
            mWaveShader!!.setLocalMatrix(mShaderMatrix)

            // Get borderWidth.
            val borderWidth = mBorderPaint!!.strokeWidth

            // The default type is triangle.
            when (shapeType) {
                // Draw triangle
                0 -> {
                    // Currently does not support the border settings
                    val start = Point(0, height)
                    val triangle = getEquilateralTriangle(start, width, height, mTriangleDirection)
                    canvas.drawPath(triangle, mWaveBgPaint)
                    canvas.drawPath(triangle, mWavePaint)
                }
                // Draw circle
                1 -> {
                    if (borderWidth > 0) {
                        canvas.drawCircle(width / 2f, height / 2f,
                                (width - borderWidth) / 2f - 1f, mBorderPaint)
                    }

                    val radius = width / 2f - borderWidth
                    // Draw background
                    canvas.drawCircle(width / 2f, height / 2f, radius, mWaveBgPaint)
                    canvas.drawCircle(width / 2f, height / 2f, radius, mWavePaint)
                }
                // Draw square
                2 -> {
                    if (borderWidth > 0) {
                        canvas.drawRect(
                                borderWidth / 2f,
                                borderWidth / 2f,
                                width.toFloat() - borderWidth / 2f - 0.5f,
                                height.toFloat() - borderWidth / 2f - 0.5f,
                                mBorderPaint)
                    }

                    canvas.drawRect(borderWidth, borderWidth, width - borderWidth,
                            height - borderWidth, mWaveBgPaint)
                    canvas.drawRect(borderWidth, borderWidth, width - borderWidth,
                            height - borderWidth, mWavePaint)
                }
                // Draw rectangle
                3 -> if (mIsRoundRectangle) {
                    if (borderWidth > 0) {
                        val rect = RectF(borderWidth / 2f, borderWidth / 2f, width.toFloat() - borderWidth / 2f - 0.5f, height.toFloat() - borderWidth / 2f - 0.5f)
                        canvas.drawRoundRect(rect, mRoundRectangleXY.toFloat(), mRoundRectangleXY.toFloat(), mBorderPaint)
                        canvas.drawRoundRect(rect, mRoundRectangleXY.toFloat(), mRoundRectangleXY.toFloat(), mWaveBgPaint)
                        canvas.drawRoundRect(rect, mRoundRectangleXY.toFloat(), mRoundRectangleXY.toFloat(), mWavePaint)
                    } else {
                        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
                        canvas.drawRoundRect(rect, mRoundRectangleXY.toFloat(), mRoundRectangleXY.toFloat(), mWaveBgPaint)
                        canvas.drawRoundRect(rect, mRoundRectangleXY.toFloat(), mRoundRectangleXY.toFloat(), mWavePaint)
                    }
                } else {
                    if (borderWidth > 0) {
                        canvas.drawRect(borderWidth / 2f, borderWidth / 2f, width.toFloat() - borderWidth / 2f - 0.5f, height.toFloat() - borderWidth / 2f - 0.5f, mWaveBgPaint)
                        canvas.drawRect(borderWidth / 2f, borderWidth / 2f, width.toFloat() - borderWidth / 2f - 0.5f, height.toFloat() - borderWidth / 2f - 0.5f, mWavePaint)
                    } else {
                        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), mWaveBgPaint)
                        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), mWavePaint)
                    }
                }
                else -> {
                }
            }

            // I know, the code written here is very shit.
            if (!TextUtils.isEmpty(topTitle)) {
                val top = mTopTitlePaint!!.measureText(topTitle)
                // Draw the stroke of top text
                canvas.drawText(topTitle, (width - top) / 2,
                        height * 2 / 10.0f, mTopTitleStrokePaint)
                // Draw the top text
                canvas.drawText(topTitle, (width - top) / 2,
                        height * 2 / 10.0f, mTopTitlePaint)
            }

            if (!TextUtils.isEmpty(centerTitle)) {
                val middle = mCenterTitlePaint!!.measureText(centerTitle)
                // Draw the stroke of centered text
//                canvas.drawText(centerTitle, (width - middle) / 2,
//                        height / 2 - (mCenterTitleStrokePaint!!.descent() + mCenterTitleStrokePaint!!.ascent()) / 2, mCenterTitleStrokePaint)
                // Draw the centered text
                canvas.drawText(centerTitle, (width - middle) / 2,
                        height / 2 - (mCenterTitlePaint!!.descent() + mCenterTitlePaint!!.ascent()) / 2, mCenterTitlePaint)
            }

            if (!TextUtils.isEmpty(bottomTitle)) {
                val bottom = mBottomTitlePaint!!.measureText(bottomTitle)
                // Draw the stroke of bottom text
                canvas.drawText(bottomTitle, (width - bottom) / 2,
                        height * 8 / 10.0f - (mBottomTitleStrokePaint!!.descent() + mBottomTitleStrokePaint!!.ascent()) / 2, mBottomTitleStrokePaint)
                // Draw the bottom text
                canvas.drawText(bottomTitle, (width - bottom) / 2,
                        height * 8 / 10.0f - (mBottomTitlePaint!!.descent() + mBottomTitlePaint!!.ascent()) / 2, mBottomTitlePaint)
            }
        } else {
            mWavePaint!!.shader = null
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // If shapType is rectangle
        if (shapeType == 3) {
            mCanvasWidth = w
            mCanvasHeight = h
        } else {
            mCanvasSize = w
            if (h < mCanvasSize)
                mCanvasSize = h
        }
        updateWaveShader()
    }

    private fun updateWaveShader() {
        // IllegalArgumentException: width and height must be > 0 while loading Bitmap from View
        // http://stackoverflow.com/questions/17605662/illegalargumentexception-width-and-height-must-be-0-while-loading-bitmap-from
        if (bitmapBuffer == null || haveBoundsChanged()) {
            bitmapBuffer?.recycle()
            val width = measuredWidth
            val height = measuredHeight
            if (width > 0 && height > 0) {
                val defaultAngularFrequency = 2.0f * Math.PI / DEFAULT_WAVE_LENGTH_RATIO.toDouble() / width.toDouble()
                val defaultAmplitude = height * DEFAULT_AMPLITUDE_RATIO
                mDefaultWaterLevel = height * DEFAULT_WATER_LEVEL_RATIO
                val defaultWaveLength = width.toFloat()

                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)

                val wavePaint = Paint()
                wavePaint.strokeWidth = 2f
                wavePaint.isAntiAlias = true

                // Draw default waves into the bitmap.
                // y=Asin(ωx+φ)+h
                val endX = width + 1
                val endY = height + 1

                val waveY = FloatArray(endX)

                wavePaint.color = adjustAlpha(mWaveColor, 0.3f)
                for (beginX in 0 until endX) {
                    val wx = beginX * defaultAngularFrequency
                    val beginY = (mDefaultWaterLevel + defaultAmplitude * Math.sin(wx)).toFloat()
                    canvas.drawLine(beginX.toFloat(), beginY, beginX.toFloat(), endY.toFloat(), wavePaint)
                    waveY[beginX] = beginY
                }

                wavePaint.color = mWaveColor
                val wave2Shift = (defaultWaveLength / 4).toInt()
                for (beginX in 0 until endX) {
                    canvas.drawLine(beginX.toFloat(), waveY[(beginX + wave2Shift) % endX], beginX.toFloat(), endY.toFloat(), wavePaint)
                }

                // Use the bitamp to create the shader.
                mWaveShader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP)
                this.mWavePaint!!.shader = mWaveShader
            }
        }
    }

    private fun haveBoundsChanged(): Boolean {
        return measuredWidth != bitmapBuffer!!.width || measuredHeight != bitmapBuffer.height
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = measureWidth(widthMeasureSpec)
        val height = measureHeight(heightMeasureSpec)
        // If shapType is rectangle
        if (shapeType == 3) {
            setMeasuredDimension(width, height)
        } else {
            val imageSize = if (width < height) width else height
            setMeasuredDimension(imageSize, imageSize)
        }

    }

    private fun measureWidth(measureSpec: Int): Int {
        val result: Int
        val specMode = View.MeasureSpec.getMode(measureSpec)
        val specSize = View.MeasureSpec.getSize(measureSpec)

        if (specMode == View.MeasureSpec.EXACTLY) {
            // The parent has determined an exact size for the child.
            result = specSize
        } else if (specMode == View.MeasureSpec.AT_MOST) {
            // The child can be as large as it wants up to the specified size.
            result = specSize
        } else {
            // The parent has not imposed any constraint on the child.
            result = mCanvasWidth
        }
        return result
    }

    private fun measureHeight(measureSpecHeight: Int): Int {
        val result: Int
        val specMode = View.MeasureSpec.getMode(measureSpecHeight)
        val specSize = View.MeasureSpec.getSize(measureSpecHeight)

        if (specMode == View.MeasureSpec.EXACTLY) {
            // We were told how big to be.
            result = specSize
        } else if (specMode == View.MeasureSpec.AT_MOST) {
            // The child can be as large as it wants up to the specified size.
            result = specSize
        } else {
            // Measure the text (beware: ascent is a negative number).
            result = mCanvasHeight
        }
        return result + 2
    }

    fun setShapeType(shapeType: ShapeType) {
        this.shapeType = shapeType.ordinal
        invalidate()
    }

    /**
     * Set vertical size of wave according to amplitudeRatio.
     *
     * @param amplitudeRatio Default to be 0.05. Result of amplitudeRatio + waterLevelRatio should be less than 1.
     */
    fun setAmplitudeRatio(amplitudeRatio: Int) {
        if (this.amplitudeRatio != amplitudeRatio.toFloat() / 1000) {
            this.amplitudeRatio = amplitudeRatio.toFloat() / 1000
            invalidate()
        }
    }

    fun setTopTitleSize(topTitleSize: Float) {
        mTopTitlePaint!!.textSize = sp2px(topTitleSize).toFloat()
    }

    fun getsetTopTitleSize(): Float {
        return mTopTitlePaint!!.textSize
    }

    fun setTopTitleStrokeWidth(topTitleStrokeWidth: Float) {
        mTopTitleStrokePaint!!.strokeWidth = dp2px(topTitleStrokeWidth).toFloat()
    }

    fun setTopTitleStrokeColor(topTitleStrokeColor: Int) {
        mTopTitleStrokePaint!!.color = topTitleStrokeColor
    }

    fun setBottomTitleStrokeWidth(bottomTitleStrokeWidth: Float) {
        mBottomTitleStrokePaint!!.strokeWidth = dp2px(bottomTitleStrokeWidth).toFloat()
    }

    fun setBottomTitleStrokeColor(bottomTitleStrokeColor: Int) {
        mBottomTitleStrokePaint!!.color = bottomTitleStrokeColor
    }

//    fun setCenterTitleStrokeWidth(centerTitleStrokeWidth: Float) {
//        mCenterTitleStrokePaint!!.strokeWidth = dp2px(centerTitleStrokeWidth).toFloat()
//    }

    fun setCenterTitleStrokeColor(centerTitleStrokeColor: Int, strokeSizeWidth: Float) {
        mCenterTitlePaint!!.setShadowLayer(strokeSizeWidth, 5F, 5F, centerTitleStrokeColor)

    }

    fun startAnimation() {
        if (mAnimatorSet != null) {
            mAnimatorSet!!.start()
        }
    }

    fun endAnimation() {
        if (mAnimatorSet != null) {
            mAnimatorSet!!.end()
        }
    }

    fun cancelAnimation() {
        if (mAnimatorSet != null) {
            mAnimatorSet!!.cancel()
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun pauseAnimation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (mAnimatorSet != null) {
                mAnimatorSet!!.pause()
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun resumeAnimation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (mAnimatorSet != null) {
                mAnimatorSet!!.resume()
            }
        }
    }

    /**
     * Sets the length of the animation. The default duration is 1000 milliseconds.
     *
     * @param duration The length of the animation, in milliseconds.
     */
    fun setAnimDuration(duration: Long) {
        waveShiftAnim!!.duration = duration
    }

    private fun initAnimation() {
        // Wave waves infinitely.
        waveShiftAnim = ObjectAnimator.ofFloat(this, "waveShiftRatio", 0f, 1f)
        waveShiftAnim!!.repeatCount = ValueAnimator.INFINITE
        waveShiftAnim!!.duration = 1000
        waveShiftAnim!!.interpolator = LinearInterpolator()
        mAnimatorSet = AnimatorSet()
        mAnimatorSet!!.play(waveShiftAnim)
    }

    override fun onAttachedToWindow() {
        startAnimation()
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        cancelAnimation()
        super.onDetachedFromWindow()
    }

    /**
     * Transparent the given color by the factor
     * The more the factor closer to zero the more the color gets transparent
     *
     * @param color  The color to transparent
     * @param factor 1.0f to 0.0f
     * @return int - A transplanted color
     */
    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    /**
     * Paint.setTextSize(float textSize) default unit is px.
     *
     * @param spValue The real size of text
     * @return int - A transplanted sp
     */
    private fun sp2px(spValue: Float): Int {
        val fontScale = mContext!!.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }

    private fun dp2px(dp: Float): Int {
        val scale = mContext!!.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    /**
     * Draw EquilateralTriangle
     *
     * @param p1        Start point
     * @param width     The width of triangle
     * @param height    The height of triangle
     * @param direction The direction of triangle
     * @return Path
     */
    private fun getEquilateralTriangle(p1: Point, width: Int, height: Int, direction: Int): Path {
        var p2: Point? = null
        var p3: Point? = null
        // NORTH
        if (direction == 0) {
            p2 = Point(p1.x + width, p1.y)
            p3 = Point(p1.x + width / 2, (height - Math.sqrt(3.0) / 2 * height).toInt())
        } else if (direction == 1) {
            p2 = Point(p1.x, p1.y - height)
            p3 = Point(p1.x + width, p1.y - height)
            p1.x = p1.x + width / 2
            p1.y = (Math.sqrt(3.0) / 2 * height).toInt()
        } else if (direction == 2) {
            p2 = Point(p1.x, p1.y - height)
            p3 = Point((Math.sqrt(3.0) / 2 * width).toInt(), p1.y / 2)
        } else if (direction == 3) {
            p2 = Point(p1.x + width, p1.y - height)
            p3 = Point(p1.x + width, p1.y)
            p1.x = (width - Math.sqrt(3.0) / 2 * width).toInt()
            p1.y = p1.y / 2
        }// WEST
        // EAST
        // SOUTH

        val path = Path()
        path.moveTo(p1.x.toFloat(), p1.y.toFloat())
        path.lineTo(p2!!.x.toFloat(), p2.y.toFloat())
        path.lineTo(p3!!.x.toFloat(), p3.y.toFloat())

        return path
    }

    enum class ShapeType {
        TRIANGLE,
        CIRCLE,
        SQUARE,
        RECTANGLE
    }

    enum class TriangleDirection {
        NORTH,
        SOUTH,
        EAST,
        WEST
    }

    companion object {
        /**
         * +------------------------+
         * | wave length - 波长      |__________
         * |   /\          |   /\   |  |
         * |  /  \         |  /  \  | amplitude - 振幅
         * | /    \        | /    \ |  |
         * |/      \       |/      \|__|_______
         * |        \      /        |  |
         * |         \    /         |  |
         * |          \  /          |  |
         * |           \/           | water level - 水位
         * |                        |  |
         * |                        |  |
         * +------------------------+__|_______
         */
        private val DEFAULT_AMPLITUDE_RATIO = 0.1f
        private val DEFAULT_AMPLITUDE_VALUE = 50.0f
        private val DEFAULT_WATER_LEVEL_RATIO = 0.5f
        private val DEFAULT_WAVE_LENGTH_RATIO = 1.0f
        private val DEFAULT_WAVE_SHIFT_RATIO = 0.0f
        private val DEFAULT_WAVE_PROGRESS_VALUE = 50
        private val DEFAULT_WAVE_COLOR = Color.parseColor("#212121")
        private val DEFAULT_WAVE_BACKGROUND_COLOR = Color.parseColor("#00000000")
        private val DEFAULT_TITLE_COLOR = Color.parseColor("#212121")
        private val DEFAULT_STROKE_COLOR = Color.TRANSPARENT
        private val DEFAULT_BORDER_WIDTH = 0f
        private val DEFAULT_TITLE_STROKE_WIDTH = 0f
        // This is incorrect/not recommended by Joshua Bloch in his book Effective Java (2nd ed).
        private val DEFAULT_WAVE_SHAPE = ShapeType.CIRCLE.ordinal
        private val DEFAULT_TRIANGLE_DIRECTION = TriangleDirection.NORTH.ordinal
        private val DEFAULT_ROUND_RECTANGLE_X_AND_Y = 30
        private val DEFAULT_TITLE_TOP_SIZE = 18.0f
        private val DEFAULT_TITLE_CENTER_SIZE = 22.0f
        private val DEFAULT_TITLE_BOTTOM_SIZE = 18.0f
    }
}// Constructor & Init Method.
