package com.jaye.didadida.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

/**
 * 下班撒花效果组件。
 *
 * @param trigger 计数器变化时触发撒花
 * @param modifier 修饰符
 * @param durationMs 撒花持续时间（毫秒），默认 4000
 * @param onStarted 撒花开始回调
 * @param onEnded 撒花结束回调
 */
@Composable
fun ConfettiCelebration(
    trigger: Int,
    modifier: Modifier = Modifier.fillMaxSize(),
    durationMs: Long = 4000L,
    onStarted: () -> Unit = {},
    onEnded: () -> Unit = {},
) {
    var showConfetti by remember { mutableStateOf(false) }

    LaunchedEffect(trigger) {
        if (trigger > 0 && !showConfetti) {
            showConfetti = true
            onStarted()
            delay(durationMs)
            showConfetti = false
            onEnded()
        }
    }

    if (showConfetti) {
        KonfettiView(
            modifier = modifier,
            parties = listOf(
                Party(
                    speed = 0f,
                    maxSpeed = 30f,
                    damping = 0.9f,
                    spread = 360,
                    colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def, 0x4CAF50),
                    emitter = Emitter(duration = 200, TimeUnit.MILLISECONDS).max(120),
                    position = Position.Relative(0.5, 0.3),
                )
            ),
        )
    }
}
