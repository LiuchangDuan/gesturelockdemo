package com.example.administrator.gesturelockdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.TextUtils;
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

    // 矩阵
    private Matrix matrix = new Matrix();

    // 画笔
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // 9个点
    private Point[][] points = new Point[3][3];

    private boolean isInit, isSelect, isFinish, movingNoPoint;

    private float width, height, offsetsX, offsetsY, bitmapRadius, movingX, movingY;

    private Bitmap pointNormal, pointPressed, pointError, linePressed, lineError;
    // 按下的点集合
    private List<Point> pointList = new ArrayList<Point>();

    // 监听器
    private OnPatternChangedListener onPatternChangedListener;

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
        // 画点
        points2Canvas(canvas);
        // 画线
        if (pointList.size() > 0) {
            Point a = pointList.get(0);
            // 绘制九宫格里的坐标点
            for (int i = 0; i < pointList.size(); i++) {
                Point b = pointList.get(i);
                line2Canvas(canvas, a, b);
                a = b;
            }
            // 绘制鼠标坐标点
            if (movingNoPoint) {
                line2Canvas(canvas, a, new Point(movingX, movingY));
            }
        }
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
     * 画线
     * @param canvas 画布
     * @param a 第一个点
     * @param b 第二个点
     */
    private void line2Canvas(Canvas canvas, Point a, Point b) {
        // 线的长度
//        double lineLength = Point.distance(a, b);
        float lineLength = (float) Point.distance(a, b);
        float degrees = getDegrees(a, b);
        canvas.rotate(degrees, a.x, a.y);
        if (a.state == Point.STATE_PRESSED) {
            matrix.setScale(lineLength / linePressed.getWidth(), 1);
            matrix.postTranslate(a.x - linePressed.getWidth() / 2, a.y - linePressed.getHeight() / 2);
            canvas.drawBitmap(linePressed, matrix, paint);
        } else {
            matrix.setScale(lineLength / lineError.getWidth(), 1);
            matrix.postTranslate(a.x - lineError.getWidth() / 2, a.y - lineError.getHeight() / 2);
            canvas.drawBitmap(lineError, matrix, paint);
        }
        canvas.rotate(-degrees, a.x, a.y);
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
        points[0][2] = new Point(offsetsX + width - width / 4, offsetsY + width / 4);

        points[1][0] = new Point(offsetsX + width / 4, offsetsY + width / 2);
        points[1][1] = new Point(offsetsX + width / 2, offsetsY + width / 2);
        points[1][2] = new Point(offsetsX + width - width / 4, offsetsY + width / 2);

        points[2][0] = new Point(offsetsX + width / 4, offsetsY + width - width / 4);
        points[2][1] = new Point(offsetsX + width / 2, offsetsY + width - width / 4);
        points[2][2] = new Point(offsetsX + width - width / 4, offsetsY + width - width / 4);

        // 5.图片资源的半径
        bitmapRadius = pointNormal.getHeight() / 2;

        // 6.设置密码
        int index  = 1;
        for (Point[] points : this.points) {
            for (Point point : points) {
                point.index = index;
                index++;
            }
        }

        // 7.初始化完成
        isInit = true;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        movingNoPoint = false;
        isFinish = false;
        movingX = event.getX();
        movingY = event.getY();
        Point point = null;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 重新绘制
                if (onPatternChangedListener != null) {
                    onPatternChangedListener.onPatternStart((true));
                }

                resetPoint();
                point = checkSelectPoint();
                if (point != null) {
                    isSelect = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isSelect) {
                    point = checkSelectPoint();
                    if (point == null) {
                        movingNoPoint = true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                isFinish = true;
                isSelect = false;
                break;
        }
        // 选中重复检查
        if (!isFinish && isSelect && point != null) {
            // 交叉点
            if (crossPoint(point)) {
                movingNoPoint = true;
            // 新点
            } else {
                point.state = Point.STATE_PRESSED;
                pointList.add(point);
            }

        }
        // 绘制结束
        if (isFinish) {
            // 只有一个点，绘制不成立
            if (pointList.size() == 1) {
                resetPoint();
            // 绘制错误
            } else if (pointList.size() < POINT_SIZE && pointList.size() > 1) {
                errorPoint();
                if (onPatternChangedListener != null) {
//                    String passwordStr = "";
//                    for (int i = 0; i < pointList.size(); i++) {
//                        passwordStr = passwordStr + pointList.get(i).index;
//                    }
                    onPatternChangedListener.onPatternChanged(null);
                }
            // 绘制成功
            } else {
                if (onPatternChangedListener != null) {
                    String passwordStr = "";
                    for (int i = 0; i < pointList.size(); i++) {
                        passwordStr = passwordStr + pointList.get(i).index;
                    }
                    if (!TextUtils.isEmpty(passwordStr)) {
                        onPatternChangedListener.onPatternChanged(passwordStr);
                    }
                }
            }
        }
        // 刷新View
        postInvalidate();
        return true;
    }

    /**
     * 交叉点
     * @param point 点
     * @return 是否交叉
     */
    private boolean crossPoint(Point point) {
        if (pointList.contains(point)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 设置绘制不成立
     */
    public void resetPoint() {
        for (int i = 0; i < pointList.size(); i++) {
            Point point = pointList.get(i);
            point.state = Point.STATE_NORMAL;
        }
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

    public float getDegrees(Point a, Point b) {
        float ax = a.x;
        float ay = a.y;
        float bx = b.x;
        float by = b.y;
        float degrees = 0;

        // y轴相等 90度或270
        if (bx == ax) {
            // 在y轴的下边 90
            if (by > ay) {
                degrees = 90;
            // 在y轴的上边 270
            } else if (by < ay) {
                degrees = 270;
            }
        // y轴相等 0度或180
        } else if (by == ay) {
            // 在y轴的下边 90
            if (bx > ax) {
                degrees = 0;
            // 在y轴的上边 270
            } else if (bx < ax) {
                degrees = 180;
            }
        } else {
            // 在y轴的右边 270~90
            if (bx > ax) {
                // 在y轴的下边 0~90
                if (by > ay) {
                    degrees = 0;
                    degrees = degrees + switchDegrees(Math.abs(by - ay), Math.abs(bx - ax));
//                    degrees = degrees + switchDegrees(Math.abs(bx - ax), Math.abs(by - ay));
                // 在y轴的上边 270~0
                } else if (by < ay) {
                    degrees = 360;
                    degrees = degrees - switchDegrees(Math.abs(by - ay), Math.abs(bx - ax));
//                    degrees = degrees - switchDegrees(Math.abs(bx - ax), Math.abs(by - ay));
                }
            // 在y轴的左边 90~270
            } else if (bx < ax) {
                // 在y轴的下边 180~270
                if (by > ay) {
                    degrees = 90;
                    degrees = degrees + switchDegrees(Math.abs(bx - ax), Math.abs(by - ay));
                // 在y轴的上边 90~180
                } else if (by < ay) {
                    degrees = 270;
                    degrees = degrees - switchDegrees(Math.abs(bx - ax), Math.abs(by - ay));
                }
            }
        }
        return degrees;
    }

    private float switchDegrees(float x, float y) {
        // 弧度转化为角度
        return (float) Math.toDegrees(Math.atan2(x, y));
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

    /**
     * 图案监听器
     */
    public static interface OnPatternChangedListener {
        /**
         * 图案改变
         * @param passwordStr 图案密码
         */
        void onPatternChanged(String passwordStr);

        /**
         * 图案重新绘制
         * @param isStart 是否重新绘制
         */
        void onPatternStart(boolean isStart);
    }

    /**
     *  设置图案的监听器
     * @param onPatternChangedListener
     */
    public void setOnPatternChangedListener(OnPatternChangedListener onPatternChangedListener) {
        if (onPatternChangedListener != null) {
            this.onPatternChangedListener = onPatternChangedListener;
        }
    }

}
