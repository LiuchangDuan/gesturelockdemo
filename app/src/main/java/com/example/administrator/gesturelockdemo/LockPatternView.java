package com.example.administrator.gesturelockdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/4/24.
 */
public class LockPatternView extends View {
    // 选中点的数量
    private static final int POINT_SIZE = 5;

    // 画笔
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // 9个点
    private Point[][] points = new Point[3][3];

    private boolean isInit, isSelect, isFinish;

    private float width, height, offsetsX, offsetsY, bitmapRadius, movingX, movingY;

    private Bitmap pointNormal, pointPressed, pointError, linePressed, lineError;
    // 按下的点集合
    private List<Point> pointList = new ArrayList<Point>();

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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        movingX = event.getX();
        movingY = event.getY();

        Point point = null;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                resetPoint();
                point = checkSelectPoint();
                if (point != null) {
                    isSelect = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isSelect) {
                    point = checkSelectPoint();
                }
                break;
            case MotionEvent.ACTION_UP:
                isFinish = true;
                isSelect = false;
                break;

        }
        // 选中重复检查
        if (!isFinish && isSelect && point != null) {

        }
        // 绘制结束
        if (isFinish) {
            // 只有一个点，绘制不成立
            if (pointList.size() == 1) {
                resetPoint();
            // 绘制错误
            } else if (pointList.size() < POINT_SIZE && pointList.size() > 2) {
                errorPoint();
            }
        }
        // 刷新View
        postInvalidate();
        return true;
    }

    /**
     * 设置绘制不成立
     */
    public void resetPoint() {
        pointList.clear();
    }

    /**
     * 设置绘制错误
     */
    public void errorPoint() {
        for (Point point : pointList) {
            point.state = Point.STATE_ERROR;
        }
    }

    /**
     * 检查是否选中
     * @return
     */
    private Point checkSelectPoint() {

        for (int i = 0; i < points.length; i++) {
            for (int j = 0; j < points[i].length; j++) {
                Point point = points[i][j];
                if (Point.with(point.x, point.y, bitmapRadius, movingX, movingY)) {
                    return point;
                }
            }
        }

        return null;
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

        /**
         * 两点之间的距离
         * @param a 点a
         * @param b 点b
         * @return 距离
         */
        public static double distance(Point a, Point b) {
            // x轴差的平方加上y轴差的平方，然后对和开方
            return Math.sqrt(Math.abs(a.x - b.x) * Math.abs(a.x - b.x) + Math.abs(a.y - b.y) * Math.abs(a.y - b.y));
        }

        /**
         * 是否重合
         * @param pointX 参考点的x
         * @param pointY 参考点的y
         * @param r      圆的半径
         * @param movingX 移动点的x
         * @param movingY 移动点的y
         * @return 是否重合
         */
        public static boolean with(float pointX, float pointY, float r, float movingX, float movingY) {
            // 开方
            return Math.sqrt((pointX - movingX) * (pointX - movingX) + (pointY - movingY) * (pointY - movingY)) < r;
        }


    }

}
