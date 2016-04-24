package com.example.administrator.gesturelockdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Administrator on 2016/4/24.
 */
public class LockPatternView extends View {
    // 画笔
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // 9个点
    private Point[][] points = new Point[3][3];

    private boolean isInit;

    private float width, height, offsetsX, offsetsY, bitmapRadius;

    private Bitmap pointNormal, pointPressed, pointError, linePressed, lineError;

    public LockPatternView(Context context) {
        super(context);
    }

    public LockPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LockPatternView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isInit) {
            initPoints();
        }
        points2Canvas(canvas);
    }

    /**
     *  将点绘制到画布
     * @param canvas 画布
     */
    private void points2Canvas(Canvas canvas) {
        for (int i = 0; i < points.length; i++) {
            for (int j = 0; j < points[i].length; j++) {
                Point point = points[i][j];
                if (point.state == Point.STATE_PRESSED) {
                    canvas.drawBitmap(pointPressed, point.x - bitmapRadius, point.y - bitmapRadius, paint);
                } else if (point.state == Point.STATE_ERROR) {
                    canvas.drawBitmap(pointError, point.x - bitmapRadius, point.y - bitmapRadius, paint);
                } else {
                    canvas.drawBitmap(pointNormal, point.x - bitmapRadius, point.y - bitmapRadius, paint);
                }
            }
        }
    }

    /**
     *  初始化点
     */
    private void initPoints() {
        // 1.获取布局宽高
        width = getWidth();
        height = getHeight();
        // 2.偏移量
        // 横屏
        if (width > height) {
            offsetsX = (width - height) / 2;
            width = height;
        // 竖屏
        } else {
            offsetsY = (height - width) / 2;
            height = width;
        }

        // 3.图片资源
        pointNormal = BitmapFactory.decodeResource(getResources(), R.drawable.bitmap_normal);
        pointPressed = BitmapFactory.decodeResource(getResources(), R.drawable.bitmap_pressed);
        pointError = BitmapFactory.decodeResource(getResources(), R.drawable.bitmap_error);
        linePressed = BitmapFactory.decodeResource(getResources(), R.drawable.line_pressed);
        lineError = BitmapFactory.decodeResource(getResources(), R.drawable.line_error);

        // 4.点的坐标
        points[0][0] = new Point(offsetsX + width / 4, offsetsY + width / 4);
        points[0][1] = new Point(offsetsX + width / 2, offsetsY + width / 4);
        points[0][2] = new Point(offsetsX + width - width / 4, offsetsY + width - width / 4);

        points[1][0] = new Point(offsetsX + width / 4, offsetsY + width / 2);
        points[1][1] = new Point(offsetsX + width / 2, offsetsY + width / 2);
        points[1][2] = new Point(offsetsX + width - width / 4, offsetsY + width / 2);

        points[2][0] = new Point(offsetsX + width / 4, offsetsY + width - width / 4);
        points[2][1] = new Point(offsetsX + width / 2, offsetsY + width - width / 4);
        points[2][2] = new Point(offsetsX + width - width / 4, offsetsY + width - width / 4);

        // 5.图片资源的半径
        bitmapRadius = pointNormal.getHeight() / 2;

    }

    /**
     *  自定义的点
     */
    public static class Point {
        // 正常
        public static int STATE_NORMAL = 0;
        // 选中
        public static int STATE_PRESSED = 1;
        // 错误
        public static int STATE_ERROR = 2;
        public float x, y;
        public int index = 0, state = 0;
        public Point() {}

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

}
