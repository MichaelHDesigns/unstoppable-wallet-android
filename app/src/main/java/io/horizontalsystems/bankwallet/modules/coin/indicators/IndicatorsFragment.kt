package io.horizontalsystems.bankwallet.modules.coin.indicators

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.findNavController
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.core.BaseFragment
import io.horizontalsystems.bankwallet.core.slideFromRight
import io.horizontalsystems.bankwallet.modules.chart.ChartIndicator
import io.horizontalsystems.bankwallet.ui.compose.ComposeAppTheme
import io.horizontalsystems.bankwallet.ui.compose.TranslatableString
import io.horizontalsystems.bankwallet.ui.compose.components.*
import io.horizontalsystems.chartview.models.ChartIndicatorType

class IndicatorsFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )
            setContent {
                ComposeAppTheme {
                    IndicatorsScreen(
                        navController = findNavController(),
                    )
                }
            }
        }
    }
}

@Composable
fun IndicatorsScreen(navController: NavController) {
    val chartIndicatorsViewModel = viewModel<ChartIndicatorsViewModel>(factory = ChartIndicatorsViewModel.Factory())
    var showDataError by remember { mutableStateOf(true) }

    val uiState = chartIndicatorsViewModel.uiState
    val toggleIndicator = { indicatorId: String, checked: Boolean ->
        if (checked) {
            chartIndicatorsViewModel.enable(indicatorId)
        } else {
            chartIndicatorsViewModel.disable(indicatorId)
        }
    }

    Scaffold(
        backgroundColor = ComposeAppTheme.colors.tyler,
        topBar = {
            AppBar(
                title = TranslatableString.ResString(R.string.CoinPage_Indicators),
                navigationIcon = {
                    HsBackButton(onClick = { navController.popBackStack() })
                }
            )
        }
    ) {
        Column(Modifier.padding(it)) {
            HeaderText(
                stringResource(R.string.CoinPage_MovingAverages).uppercase()
            )
            CellUniversalLawrenceSection(uiState.maIndicators) { indicator: ChartIndicator ->
                val movingAverage = indicator.indicatorType as ChartIndicatorType.MovingAverage
                IndicatorCell(
                    title = indicator.name,
                    checked = indicator.enabled,
                    leftIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_chart_type_2_24),
                            tint = Color(android.graphics.Color.parseColor(movingAverage.color)),
                            contentDescription = null,
                        )
                    },
                    onCheckedChange = {
                        toggleIndicator.invoke(indicator.id, it)
                    },
                    onEditClick = {
                        navController.slideFromRight(R.id.emaSettingsFragment)
                    }
                )
            }
            if (showDataError) {
                VSpacer(12.dp)
                TextImportantError(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    title = stringResource(R.string.CoinPage_InsufficientData),
                    text = stringResource(R.string.CoinPage_InsufficientDataError),
                    icon = R.drawable.ic_attention_20
                )
            }
            VSpacer(24.dp)
            HeaderText(
                stringResource(R.string.CoinPage_OscillatorsSettings).uppercase()
            )
            CellUniversalLawrenceSection(uiState.oscillatorIndicators) { indicator ->
                IndicatorCell(
                    title = indicator.name,
                    checked = indicator.enabled,
                    onCheckedChange = {
                        toggleIndicator.invoke(indicator.id, it)
                    },
                    onEditClick = {
                        navController.slideFromRight(R.id.rsiSettingsFragment)
                    }
                )
            }
        }
    }
}

@Composable
private fun IndicatorCell(
    title: String,
    checked: Boolean,
    leftIcon: (@Composable () -> Unit)? = null,
    onCheckedChange: (Boolean) -> Unit,
    onEditClick: () -> Unit
) {
    RowUniversal(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        leftIcon?.invoke()
        HSpacer(16.dp)
        body_leah(
            text = title,
            modifier = Modifier.weight(1f)
        )
        HsIconButton(
            modifier = Modifier.size(20.dp),
            onClick = onEditClick
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_edit_20),
                contentDescription = null,
                tint = ComposeAppTheme.colors.grey
            )
        }
        HSpacer(16.dp)
        HsSwitch(
            modifier = Modifier.padding(0.dp),
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Preview
@Composable
private fun Preview_Indicators() {
    val navController = rememberNavController()
    ComposeAppTheme {
        IndicatorsScreen(navController)
    }
}