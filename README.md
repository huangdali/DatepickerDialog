# DatepickerDialog [![](https://jitpack.io/v/huangdali/DatepickerDialog.svg)](https://jitpack.io/#huangdali/DatepickerDialog)

日期选择器，日历选择器，可标记某一天，DatepickerDialog,Tagged calendar

- 单选某一天
- 标记某一天
- 今天之后不可点击（灰色）
- 选择回调（未标记回调）
- 自动记录上一次的选择
- 可限制只显示两个月（默认不限制）

## How to

To get a Git project into your build:

### Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
### Step 2. Add the dependency

	dependencies {
	        compile 'com.github.huangdali:DatepickerDialog:v1.0.4'
	}

### 效果图：

### 初始状态
![](https://github.com/huangdali/DatepickerDialog/blob/master/images/image.png)

### 选中状态
![](https://github.com/huangdali/DatepickerDialog/blob/master/images/image1.png)

### 切换月份
![](https://github.com/huangdali/DatepickerDialog/blob/master/images/image3.png)

### DMEO
```java
 CalendarViewDialog.getInstance()
                .init(this)
                .addMarks(markDays)
                .setLimitMonth(true)
                .show(new CalendarView.OnCalendarClickListener() {
                    @Override
                    public void onDayClick(Calendar daySelectedCalendar) {
                        CalendarViewDialog.getInstance().close();
                        Toast.makeText(MainActivity.this, "选择的天数 : " + DateUtils.getDateTime(daySelectedCalendar.getTimeInMillis()), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDayNotMarkClick(Calendar daySelectedCalendar) {
                        Toast.makeText(MainActivity.this, "当前时间无回放（没有标记）", Toast.LENGTH_SHORT).show();
                    }
                });
```

### markDays格式
```java
private List<Long>  markDays = new ArrayList<>();
```


模拟数据：

```java
for (int i = 0; i < 5; i++) {
            markDays.add(System.currentTimeMillis() - i * 24 * 60 * 60 * 1000);
}
```
