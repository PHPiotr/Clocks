package pl.nazwa.phpiotr.clocks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

@SuppressLint("ClickableViewAccessibility")
public class MainActivity extends AppCompatActivity implements OnTouchListener {

    MyAnalogClock clock;

    Time time;

    float eventX, eventY = 0;

    int hourFormat, minsFormat, secsFormat, currentHour, currentMinute,
            currentSecond;
    int canvasWidth, canvasHeight, clockWidth, clockLeft,
            clockTop, minuteHeight, minuteWidth, secondHeight, secondWidth,
            minuteLeft, minuteTop, secondLeft, secondTop, hourHeight,
            hourWidth, hourLeft, hourTop, minuteCenterX, minuteCenterY,
            secondCenterX, secondCenterY, minuteDegrees, hourDegrees,
            secondDegrees, minsHelper, hourHelper = 0;

    int q1, q2, q3, q4 = 0;

    // Clock is working by default
    boolean timeRuns = true;

    Runnable r, realTimeRunner = null;

    Handler h, realTimeHandler = null;

    protected void onSaveInstanceState(Bundle outState) {

        outState.putBoolean("TIME_RUNS", timeRuns);

        outState.putInt("SECOND_DEGREES", secondDegrees);
        outState.putInt("MINUTE_DEGREES", minuteDegrees);
        outState.putInt("HOUR_DEGREES", hourDegrees);

        outState.putInt("MINUTE_HELPER", minsHelper);
        outState.putInt("HOUR_HELPER", hourHelper);

        outState.putInt("MINUTE_CENTER_X", minuteCenterX);
        outState.putInt("MINUTE_CENTER_Y", minuteCenterY);

        outState.putInt("Q1", q1);
        outState.putInt("Q2", q2);
        outState.putInt("Q3", q3);
        outState.putInt("Q4", q4);

        super.onSaveInstanceState(outState);
    }

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {

            eventX = eventY = 0;

            timeRuns = savedInstanceState.getBoolean("TIME_RUNS");

            secondDegrees = savedInstanceState.getInt("SECOND_DEGREES");
            minuteDegrees = savedInstanceState.getInt("MINUTE_DEGREES");
            hourDegrees = savedInstanceState.getInt("HOUR_DEGREES");

            minsHelper = savedInstanceState.getInt("MINUTE_HELPER");
            hourHelper = savedInstanceState.getInt("HOUR_HELPER");

            minuteCenterX = savedInstanceState.getInt("MINUTE_CENTER_X");
            minuteCenterY = savedInstanceState.getInt("MINUTE_CENTER_Y");

            q1 = savedInstanceState.getInt("Q1");
            q2 = savedInstanceState.getInt("Q2");
            q3 = savedInstanceState.getInt("Q3");
            q4 = savedInstanceState.getInt("Q4");
        }

        clock = new MyAnalogClock(this);
        clock.resume();
        clock.setOnTouchListener(this);

        setContentView(clock);
    }

    protected void runCurrentTime() {

        realTimeHandler = new Handler();
        realTimeRunner = new Runnable() {

            public void run() {
                try {
                    setCurrentTime();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    realTimeHandler.postDelayed(realTimeRunner, 1000);
                }

            }
        };
        realTimeHandler.postDelayed(realTimeRunner, 1000);
    }

    protected void runTime() {

        h = new Handler();

        if (!timeRuns) {
            h.removeCallbacks(r);
        } else {
            r = new Runnable() {

                public void run() {

                    try {

                        // seconds
                        secondDegrees = secondDegrees >= 360 ? 0
                                : secondDegrees;
                        secondDegrees += 6;

                        // minutes
                        if (secondDegrees % 60 == 0) {
                            minsHelper = minsHelper >= 360 ? 0 : minsHelper;
                            minsHelper += 1;
                            minuteDegrees = minsHelper == 90 ? -270
                                    : minuteDegrees;
                            minuteDegrees += 1;
                        }

                        // hours
                        if (minuteDegrees % 12 == 0 && secondDegrees == 360) {
                            if (minsHelper % 360 == 0) {
                                hourHelper = hourHelper >= 24 ? 0 : hourHelper;
                                hourHelper += 1;
                            }
                            hourDegrees = hourDegrees == 360 ? 0 : hourDegrees;
                            hourDegrees += 1;
                        }

                        // 1st quarter
                        if (minuteDegrees >= 0 && minuteDegrees < 90) {
                            q1 = 1;
                            q2 = q3 = q4 = 0;
                        }

                        // 2nd quarter
                        if (minuteDegrees >= -270 && minuteDegrees < -180) {
                            q2 = 1;
                            q1 = q3 = q4 = 0;
                        }

                        // 3rd quarter
                        if (minuteDegrees >= -180 && minuteDegrees < -90) {
                            q3 = 1;
                            q1 = q2 = q4 = 0;
                        }

                        // 4th quarter
                        if (minuteDegrees >= -90 && minuteDegrees < 0) {
                            q4 = 1;
                            q1 = q2 = q3 = 0;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (!timeRuns) {
                            h.removeCallbacks(this);
                        } else {
                            h.postDelayed(this, 1000);
                        }
                    }
                }
            };
            if (!timeRuns) {
                h.removeCallbacks(r);
            } else {
                h.postDelayed(r, 1000);
            }
        }
    }

    public void setCurrentTime() {
        time.setToNow();
        currentHour = time.hour;
        currentMinute = time.minute;
        currentSecond = time.second;
    }

    public void setCurrentDegrees() {
        secondDegrees = currentSecond * 6;
        minuteDegrees = (int) (currentMinute * 6 + Math
                .floor(currentSecond / 10));
        hourDegrees = (int) (currentHour * 30 + Math.floor(currentMinute / 2));
    }

    @Override
    protected void onResume() {

        super.onResume();
        time = new Time();
        clock.resume();
        this.runTime();
        this.setCurrentTime();
        this.runCurrentTime();
        this.setCurrentDegrees();
    }

    @Override
    protected void onPause() {

        super.onPause();
        clock.pause();
        h.removeCallbacks(r);
    }

    public boolean onTouch(View v, MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            eventX = event.getX();
            eventY = event.getY();
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            timeRuns = false;
            h.removeCallbacks(r);
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            eventX = eventY = 0;
            timeRuns = true;
            this.runTime();
        }
        return true;
    }

    private class MyAnalogClock extends SurfaceView implements Runnable {

        SurfaceHolder holder;

        Matrix minuteMatrix, hourMatrix, secondMatrix;

        Thread thread = null;

        boolean isRunning = false;

        Bitmap clock, hour, minute, second;

        public MyAnalogClock(Context context) {

            super(context);
            holder = getHolder();

            minuteMatrix = new Matrix();
            hourMatrix = new Matrix();
            secondMatrix = new Matrix();
        }

        public void pause() {

            isRunning = false;

            while (true) {

                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }
            thread = null;
        }

        public void resume() {

            isRunning = true;

            thread = new Thread(this);
            thread.start();
        }

        @Override
        public void run() {

            while (isRunning) {

                if (!holder.getSurface().isValid()) {
                    continue;
                }

                Canvas canvas = holder.lockCanvas();

                Paint paint = new Paint();
                paint.setColor(Color.WHITE);
                paint.setStyle(Style.FILL);
                canvas.drawPaint(paint);

                clock = BitmapFactory.decodeResource(getResources(),
                        R.drawable.clock_dial);
                hour = BitmapFactory.decodeResource(getResources(),
                        R.drawable.clock_hour);
                minute = BitmapFactory.decodeResource(getResources(),
                        R.drawable.clock_minute);
                second = BitmapFactory.decodeResource(getResources(),
                        R.drawable.clock_second);

                // canvas.get... does not work!
                canvasWidth = getWidth();
                canvasHeight = getHeight();

                // height is not necessary as this is a square clock :)
                clockWidth = clock.getWidth();

                hourHeight = hour.getHeight();
                hourWidth = hour.getWidth();

                minuteHeight = minute.getHeight();
                minuteWidth = minute.getWidth();

                secondHeight = second.getHeight();
                secondWidth = second.getWidth();

                clockLeft = (canvasWidth - clockWidth) / 2;
                clockTop = (canvasHeight - clockWidth) / 2;

                hourLeft = (canvasWidth - hourWidth) / 2;
                hourTop = (canvasHeight - hourHeight) / 2;

                minuteLeft = (canvasWidth - minuteWidth) / 2;
                minuteTop = (canvasHeight - minuteHeight) / 2;

                secondLeft = (canvasWidth - secondWidth) / 2;
                secondTop = (canvasHeight - secondHeight) / 2;

                minuteCenterX = minuteLeft + minuteWidth / 2;
                minuteCenterY = minuteTop + minuteHeight / 2;

                secondCenterX = secondLeft + secondWidth / 2;
                secondCenterY = secondTop + secondHeight / 2;

                canvas.drawBitmap(clock, clockLeft, clockTop, null);

                secondMatrix.reset();
                secondMatrix.postTranslate(secondLeft, secondTop);

                minuteMatrix.reset();
                minuteMatrix.postTranslate(minuteLeft, minuteTop);

                hourMatrix.reset();
                hourMatrix.postTranslate(minuteLeft, minuteTop);

                if (eventX != 0 && eventY != 0) {

                    minuteDegrees = ((int) Math.toDegrees(Math.atan2(
                            minuteCenterY - eventY, minuteCenterX - eventX))) - 90;

                    minsHelper = minuteDegrees;

                    if (minuteDegrees < 0) {
                        minsHelper = minuteDegrees + 360;
                    }

                    // 1st quarter
                    if (minuteDegrees >= 0 && minuteDegrees < 90) {
                        q1 = 1;
                        if (minuteDegrees > 0) {
                            q2 = q3 = 0;
                        }
                    }
                    // 2nd quarter
                    if (minuteDegrees >= -270 && minuteDegrees < -180) {
                        q2 = 1;
                        if (minuteDegrees > -270) {
                            q1 = q3 = q4 = 0;
                        }
                    }
                    // 3rd quarter
                    if (minuteDegrees >= -180 && minuteDegrees < -90) {
                        q3 = 1;
                        if (minuteDegrees > -180) {
                            q1 = q2 = q4 = 0;
                        }
                    }
                    // 4th quarter
                    if (minuteDegrees >= -90 && minuteDegrees < 0) {
                        q4 = 1;
                        if (minuteDegrees > -90) {
                            q2 = q3 = 0;
                        }
                    }

                    // Clockwise move
                    if (q4 == 1) {
                        if (minuteDegrees > -1) {
                            if (hourHelper < 24) {
                                q4 = 0;
                                hourHelper++;
                            } else {
                                hourHelper = 0;
                            }
                        }
                    }

                    // Anti-clockwise move
                    if (q1 == 1) {
                        if (minuteDegrees < 0) {
                            if (hourHelper > 0) {
                                q4 = 1;
                                if (minuteDegrees < 0) {
                                    q1 = 0;
                                }
                                hourHelper--;
                            } else {
                                hourHelper = 24;
                            }
                        }
                    }

                    // What's the hour?
                    hourDegrees = (minsHelper + hourHelper * 360) / 12;
                    secondDegrees = ((minsHelper + 6) % 6) * 60;
                }
                secondMatrix.postRotate(secondDegrees, secondCenterX,
                        secondCenterY);
                minuteMatrix.postRotate(minuteDegrees, minuteCenterX,
                        minuteCenterY);
                hourMatrix
                        .postRotate(hourDegrees, minuteCenterX, minuteCenterY);

                canvas.drawBitmap(hour, hourMatrix, null);
                canvas.drawBitmap(minute, minuteMatrix, null);

                if (timeRuns) {
                    canvas.drawBitmap(second, secondMatrix, null);
                }

                paint.setColor(Color.BLACK);
                paint.setTextSize(20);

                canvas.drawText("eventX: " + (int) eventX, 10, 30, paint);
                canvas.drawText("eventY: " + (int) eventY, 10, 60, paint);

                canvas.drawText("secs degrees: " + secondDegrees, 10, 100,
                        paint);
                canvas.drawText("mins degrees: " + minuteDegrees, 10, 130,
                        paint);
                canvas.drawText("hour degrees: " + hourDegrees, 10, 160, paint);

                canvas.drawText("minsHelper: " + minsHelper, 10, 200, paint);
                canvas.drawText("hourHelper: " + hourHelper, 10, 230, paint);

                if (hourHelper == 24) {
                    hourFormat = 0;
                } else {
                    hourFormat = hourHelper;
                }

                if (minsHelper == 360) {
                    minsFormat = 0;
                } else {
                    minsFormat = minsHelper / 6;
                }

                if (secondDegrees == 360) {
                    secsFormat = 0;
                } else {
                    secsFormat = secondDegrees / 6;
                }

                // Display digital clock
                paint.setColor(Color.RED);
                paint.setTextSize(40);
                paint.setTextAlign(Align.RIGHT);

                canvas.drawText(String.format("%02d:%02d:%02d", hourFormat,
                        minsFormat, secsFormat), canvasWidth - 10, 40, paint);

                paint.setColor(Color.BLUE);
                canvas.drawText(String.format("%02d:%02d:%02d", currentHour,
                                currentMinute, currentSecond), canvasWidth - 10, 80,
                        paint);

                holder.unlockCanvasAndPost(canvas);
            }
        }
    }
}
