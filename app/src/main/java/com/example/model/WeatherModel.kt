package com.example.model

import androidx.compose.ui.graphics.Color

enum class WeatherCondition(
    val id: String,
    val tabLabel: String,
    val conditionTitle: String,
    val refractiveIndexLabel: String
) {
    SUNNY("sunny", "晴朗", "晴天 · 丁达尔暖阳", "IOR 1.52 · 暖金日冕折射"),
    WINDY("windy", "大风", "大风 · 气旋流线", "IOR 1.49 · 翡翠流体色散"),
    RAINY("rainy", "暴雨", "暴雨 · 雷暴晶滴", "IOR 1.33 · 靛蓝水滴全反射"),
    SNOWY("snowy", "暴雪", "暴雪 · 六方冰晶", "IOR 1.31 · 极地冰棱双折射")
}

enum class HourlyIconType {
    SUNNY,
    PARTLY_CLOUDY,
    SUNSET,
    CLEAR_NIGHT,
    WIND_GUST,
    LEAF_BREEZE,
    THUNDERSTORM,
    HEAVY_RAIN,
    SHOWER,
    HEAVY_SNOW,
    BLIZZARD,
    FROST_ICE
}

enum class MetricIconType {
    UV_INDEX,
    AQI_LEAF,
    SUNSET_HORIZON,
    THERMOMETER,
    WIND_SPEED,
    COMPASS_GUST,
    BAROMETER,
    VISIBILITY_EYE,
    RAINFALL_DROP,
    LIGHTNING_BOLT,
    HUMIDITY,
    SNOW_DEPTH,
    WARNING_ICE
}

data class HourlyForecastItem(
    val timeLabel: String,
    val tempC: Int,
    val iconType: HourlyIconType,
    val popLabel: String? = null
)

data class BentoMetricItem(
    val id: String,
    val label: String,
    val mainValue: String,
    val subText: String,
    val progress: Float? = null,
    val progressColors: List<Color> = emptyList(),
    val iconType: MetricIconType
)

data class DailyForecastItem(
    val dayName: String,
    val dateLabel: String,
    val conditionText: String,
    val minTempC: Int,
    val maxTempC: Int,
    val currentRatio: Float?,
    val iconType: HourlyIconType,
    val barColors: List<Color>
)

data class WeatherCardData(
    val condition: WeatherCondition,
    val city: String,
    val district: String,
    val summary: String,
    val badgeText: String,
    val currentTempC: Int,
    val lowTempC: Int,
    val highTempC: Int,
    val rangeKnobPosition: Float,
    val rangeGradientColors: List<Color>,
    val ambientOrbColor: Color,
    val skyColors: List<Color>,
    val hourlyTitle: String,
    val hourlyItems: List<HourlyForecastItem>,
    val bentoMetrics: List<BentoMetricItem>,
    val dailyForecasts: List<DailyForecastItem>,
    val opticalNote: String
)

object WeatherRepository {
    val allWeatherCards: List<WeatherCardData> = listOf(
        // 1. 晴天 (Sunny - 上海)
        WeatherCardData(
            condition = WeatherCondition.SUNNY,
            city = "上海",
            district = "浦东新区 · 陆家嘴",
            summary = "晴朗少云 · 能见度极佳",
            badgeText = "紫外线偏强",
            currentTempC = 28,
            lowTempC = 20,
            highTempC = 31,
            rangeKnobPosition = 0.73f,
            rangeGradientColors = listOf(
                Color(0xFF38BDF8),
                Color(0xFFFACC15),
                Color(0xFFF97316)
            ),
            ambientOrbColor = Color(0xFFF59E0B),
            skyColors = listOf(
                Color(0xFF0C4A6E),
                Color(0xFF0284C7),
                Color(0xFF38BDF8)
            ),
            hourlyTitle = "逐小时晴空趋势",
            hourlyItems = listOf(
                HourlyForecastItem("现在", 28, HourlyIconType.SUNNY),
                HourlyForecastItem("15时", 30, HourlyIconType.SUNNY),
                HourlyForecastItem("16时", 29, HourlyIconType.PARTLY_CLOUDY),
                HourlyForecastItem("17时", 26, HourlyIconType.SUNSET),
                HourlyForecastItem("18时", 23, HourlyIconType.CLEAR_NIGHT)
            ),
            bentoMetrics = listOf(
                BentoMetricItem(
                    id = "uv",
                    label = "紫外线指数",
                    mainValue = "8 很强",
                    subText = "建议涂抹 SPF30+ 防晒",
                    progress = 0.78f,
                    progressColors = listOf(Color(0xFF4ADE80), Color(0xFFFACC15), Color(0xFFEF4444)),
                    iconType = MetricIconType.UV_INDEX
                ),
                BentoMetricItem(
                    id = "aqi",
                    label = "空气质量 AQI",
                    mainValue = "28 优",
                    subText = "PM2.5 浓度 11 μg/m³",
                    progress = 0.24f,
                    progressColors = listOf(Color(0xFF34D399), Color(0xFF10B981)),
                    iconType = MetricIconType.AQI_LEAF
                ),
                BentoMetricItem(
                    id = "sunset",
                    label = "日落时间",
                    mainValue = "18:42",
                    subText = "黄金时刻 18:15 · 初昏 19:08",
                    iconType = MetricIconType.SUNSET_HORIZON
                ),
                BentoMetricItem(
                    id = "feels_like",
                    label = "体感温度",
                    mainValue = "29°",
                    subText = "湿度 42% · 微风舒适",
                    iconType = MetricIconType.THERMOMETER
                )
            ),
            dailyForecasts = listOf(
                DailyForecastItem("今天", "10/18", "晴朗无云", 20, 31, 0.73f, HourlyIconType.SUNNY, listOf(Color(0xFF38BDF8), Color(0xFFFACC15), Color(0xFFF97316))),
                DailyForecastItem("周一", "10/19", "晴间多云", 21, 30, null, HourlyIconType.PARTLY_CLOUDY, listOf(Color(0xFF38BDF8), Color(0xFFFACC15))),
                DailyForecastItem("周二", "10/20", "秋高气爽", 19, 27, null, HourlyIconType.SUNNY, listOf(Color(0xFF38BDF8), Color(0xFFFDE047))),
                DailyForecastItem("周三", "10/21", "多云微风", 18, 25, null, HourlyIconType.PARTLY_CLOUDY, listOf(Color(0xFF38BDF8), Color(0xFF34D399))),
                DailyForecastItem("周四", "10/22", "晴朗舒适", 19, 28, null, HourlyIconType.SUNNY, listOf(Color(0xFF38BDF8), Color(0xFFFACC15)))
            ),
            opticalNote = "3D 烈日图标采用双层旋转日冕 + 径向球体高光，前景云朵注入暖金色散与 135° 斜切镜面高光。"
        ),

        // 2. 大风 (Windy - 北京)
        WeatherCardData(
            condition = WeatherCondition.WINDY,
            city = "北京",
            district = "朝阳区 · 国贸CBD",
            summary = "强劲西北风 · 碧空万里",
            badgeText = "大风蓝色预警",
            currentTempC = 16,
            lowTempC = 9,
            highTempC = 19,
            rangeKnobPosition = 0.68f,
            rangeGradientColors = listOf(
                Color(0xFF06B6D4),
                Color(0xFF2DD4BF),
                Color(0xFFA3E635)
            ),
            ambientOrbColor = Color(0xFF2DD4BF),
            skyColors = listOf(
                Color(0xFF0F172A),
                Color(0xFF134E4A),
                Color(0xFF0F766E)
            ),
            hourlyTitle = "逐小时风力与气温趋势",
            hourlyItems = listOf(
                HourlyForecastItem("现在", 16, HourlyIconType.WIND_GUST, "6级"),
                HourlyForecastItem("15时", 17, HourlyIconType.WIND_GUST, "7级"),
                HourlyForecastItem("16时", 15, HourlyIconType.LEAF_BREEZE, "6级"),
                HourlyForecastItem("17时", 13, HourlyIconType.LEAF_BREEZE, "5级"),
                HourlyForecastItem("18时", 11, HourlyIconType.CLEAR_NIGHT, "4级")
            ),
            bentoMetrics = listOf(
                BentoMetricItem(
                    id = "wind_speed",
                    label = "持续风速",
                    mainValue = "46 km/h",
                    subText = "西北风 6级 · 强风呼啸",
                    progress = 0.72f,
                    progressColors = listOf(Color(0xFF2DD4BF), Color(0xFFFACC15)),
                    iconType = MetricIconType.WIND_SPEED
                ),
                BentoMetricItem(
                    id = "gust",
                    label = "阵风峰值",
                    mainValue = "68 km/h",
                    subText = "瞬时可达 8级 阵风",
                    iconType = MetricIconType.COMPASS_GUST
                ),
                BentoMetricItem(
                    id = "pressure",
                    label = "大气压强",
                    mainValue = "1021 hPa",
                    subText = "冷高压过境 · 稳步上升",
                    iconType = MetricIconType.BAROMETER
                ),
                BentoMetricItem(
                    id = "visibility",
                    label = "大气能见度",
                    mainValue = "28 km",
                    subText = "大风吹散霾层 · 视野极佳",
                    iconType = MetricIconType.VISIBILITY_EYE
                )
            ),
            dailyForecasts = listOf(
                DailyForecastItem("今天", "10/18", "强劲西北风", 9, 19, 0.68f, HourlyIconType.WIND_GUST, listOf(Color(0xFF06B6D4), Color(0xFF2DD4BF), Color(0xFFA3E635))),
                DailyForecastItem("周一", "10/19", "微风晴朗", 8, 20, null, HourlyIconType.SUNNY, listOf(Color(0xFF06B6D4), Color(0xFFFACC15))),
                DailyForecastItem("周二", "10/20", "秋风送爽", 7, 18, null, HourlyIconType.LEAF_BREEZE, listOf(Color(0xFF06B6D4), Color(0xFF2DD4BF))),
                DailyForecastItem("周三", "10/21", "晴间多云", 9, 21, null, HourlyIconType.PARTLY_CLOUDY, listOf(Color(0xFF38BDF8), Color(0xFFFACC15))),
                DailyForecastItem("周四", "10/22", "北风渐起", 6, 15, null, HourlyIconType.WIND_GUST, listOf(Color(0xFF38BDF8), Color(0xFF2DD4BF)))
            ),
            opticalNote = "3D 大风图标采用三道相位差正弦流体玻璃风带 + 3D 翻滚翡翠叶片，呈现空气动力学折射感。"
        ),

        // 3. 暴雨 (Rainy - 广州)
        WeatherCardData(
            condition = WeatherCondition.RAINY,
            city = "广州",
            district = "天河区 · 珠江新城",
            summary = "强雷暴大雨 · 伴随密集闪电",
            badgeText = "暴雨红色预警",
            currentTempC = 22,
            lowTempC = 19,
            highTempC = 25,
            rangeKnobPosition = 0.45f,
            rangeGradientColors = listOf(
                Color(0xFF3B82F6),
                Color(0xFF6366F1),
                Color(0xFF38BDF8)
            ),
            ambientOrbColor = Color(0xFF60A5FA),
            skyColors = listOf(
                Color(0xFF090D16),
                Color(0xFF111827),
                Color(0xFF1E1B4B)
            ),
            hourlyTitle = "强降水与雷暴逐小时分布",
            hourlyItems = listOf(
                HourlyForecastItem("现在", 22, HourlyIconType.THUNDERSTORM, "100%"),
                HourlyForecastItem("15时", 21, HourlyIconType.THUNDERSTORM, "95%"),
                HourlyForecastItem("16时", 21, HourlyIconType.HEAVY_RAIN, "90%"),
                HourlyForecastItem("17时", 20, HourlyIconType.HEAVY_RAIN, "80%"),
                HourlyForecastItem("18时", 20, HourlyIconType.SHOWER, "65%")
            ),
            bentoMetrics = listOf(
                BentoMetricItem(
                    id = "rainfall",
                    label = "小时降水量",
                    mainValue = "58 mm/h",
                    subText = "短时强降水 · 警惕积水",
                    progress = 0.92f,
                    progressColors = listOf(Color(0xFF38BDF8), Color(0xFF6366F1), Color(0xFFA855F7)),
                    iconType = MetricIconType.RAINFALL_DROP
                ),
                BentoMetricItem(
                    id = "thunder",
                    label = "雷电活跃度",
                    mainValue = "极高频",
                    subText = "云地闪频次 > 18次/分",
                    iconType = MetricIconType.LIGHTNING_BOLT
                ),
                BentoMetricItem(
                    id = "humidity",
                    label = "相对湿度",
                    mainValue = "98%",
                    subText = "露点温度 21° · 水汽饱和",
                    iconType = MetricIconType.HUMIDITY
                ),
                BentoMetricItem(
                    id = "rain_vis",
                    label = "雨幕能见度",
                    mainValue = "1.1 km",
                    subText = "强降雨导致视线受阻",
                    iconType = MetricIconType.VISIBILITY_EYE
                )
            ),
            dailyForecasts = listOf(
                DailyForecastItem("今天", "10/18", "强雷暴大雨", 19, 25, 0.45f, HourlyIconType.THUNDERSTORM, listOf(Color(0xFF3B82F6), Color(0xFF6366F1))),
                DailyForecastItem("周一", "10/19", "大到暴雨", 20, 24, null, HourlyIconType.HEAVY_RAIN, listOf(Color(0xFF3B82F6), Color(0xFF38BDF8))),
                DailyForecastItem("周二", "10/20", "雷阵雨转多云", 21, 27, null, HourlyIconType.SHOWER, listOf(Color(0xFF38BDF8), Color(0xFF34D399))),
                DailyForecastItem("周三", "10/21", "多云间晴", 22, 29, null, HourlyIconType.PARTLY_CLOUDY, listOf(Color(0xFF38BDF8), Color(0xFFFACC15))),
                DailyForecastItem("周四", "10/22", "晴朗升温", 22, 30, null, HourlyIconType.SUNNY, listOf(Color(0xFF38BDF8), Color(0xFFF97316)))
            ),
            opticalNote = "3D 暴雨图标构建双层深海乌云玻璃体，内嵌同步频闪的紫金电弧光核与三颗立体折射水滴。"
        ),

        // 4. 暴雪 (Snowy - 哈尔滨)
        WeatherCardData(
            condition = WeatherCondition.SNOWY,
            city = "哈尔滨",
            district = "道里区 · 中央大街",
            summary = "鹅毛大暴雪 · 极寒冰封",
            badgeText = "暴雪橙色预警",
            currentTempC = -14,
            lowTempC = -21,
            highTempC = -9,
            rangeKnobPosition = 0.52f,
            rangeGradientColors = listOf(
                Color(0xFF818CF8),
                Color(0xFF38BDF8),
                Color(0xFFBAE6FD)
            ),
            ambientOrbColor = Color(0xFFBAE6FD),
            skyColors = listOf(
                Color(0xFF0F172A),
                Color(0xFF1E293B),
                Color(0xFF334155)
            ),
            hourlyTitle = "降雪强度与严寒趋势",
            hourlyItems = listOf(
                HourlyForecastItem("现在", -14, HourlyIconType.HEAVY_SNOW, "95%"),
                HourlyForecastItem("15时", -13, HourlyIconType.HEAVY_SNOW, "90%"),
                HourlyForecastItem("16时", -15, HourlyIconType.BLIZZARD, "85%"),
                HourlyForecastItem("17时", -17, HourlyIconType.BLIZZARD, "80%"),
                HourlyForecastItem("18时", -19, HourlyIconType.FROST_ICE, "50%")
            ),
            bentoMetrics = listOf(
                BentoMetricItem(
                    id = "snow_depth",
                    label = "新增积雪深度",
                    mainValue = "22 cm",
                    subText = "松软粉雪 · 持续堆积中",
                    progress = 0.85f,
                    progressColors = listOf(Color(0xFF38BDF8), Color(0xFFBAE6FD), Color(0xFFFFFFFF)),
                    iconType = MetricIconType.SNOW_DEPTH
                ),
                BentoMetricItem(
                    id = "wind_chill",
                    label = "风寒体感温度",
                    mainValue = "-23°",
                    subText = "户外暴露极易冻伤",
                    iconType = MetricIconType.THERMOMETER
                ),
                BentoMetricItem(
                    id = "road_ice",
                    label = "路面结冰状态",
                    mainValue = "严重结冰",
                    subText = "摩擦系数极低 · 谨慎出行",
                    iconType = MetricIconType.WARNING_ICE
                ),
                BentoMetricItem(
                    id = "snow_wind",
                    label = "东北吹雪风",
                    mainValue = "32 km/h",
                    subText = "伴有明显风吹雪现象",
                    iconType = MetricIconType.WIND_SPEED
                )
            ),
            dailyForecasts = listOf(
                DailyForecastItem("今天", "10/18", "大到暴雪", -21, -9, 0.52f, HourlyIconType.HEAVY_SNOW, listOf(Color(0xFF818CF8), Color(0xFF38BDF8), Color(0xFFBAE6FD))),
                DailyForecastItem("周一", "10/19", "阵雪转晴", -23, -11, null, HourlyIconType.BLIZZARD, listOf(Color(0xFF6366F1), Color(0xFF38BDF8))),
                DailyForecastItem("周二", "10/20", "严寒晴空", -24, -12, null, HourlyIconType.FROST_ICE, listOf(Color(0xFF6366F1), Color(0xFF7DD3FC))),
                DailyForecastItem("周三", "10/21", "多云微雪", -20, -8, null, HourlyIconType.HEAVY_SNOW, listOf(Color(0xFF818CF8), Color(0xFFBAE6FD))),
                DailyForecastItem("周四", "10/22", "晴冷冰晶", -19, -7, null, HourlyIconType.SUNNY, listOf(Color(0xFF38BDF8), Color(0xFFBAE6FD)))
            ),
            opticalNote = "3D 暴雪图标采用六轴对称冰棱雪花晶体 + 中央六角冰晶柱，搭配磨砂寒云与悬浮微晶闪烁。"
        )
    )
}
