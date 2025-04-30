package dev.shadoe.delta.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.shadoe.delta.R

@Composable
internal fun TaskerIntegrationField(
  isTaskerIntegrationEnabled: Boolean,
  onTaskerIntegrationChange: (Boolean) -> Unit,
  onShowTaskerIntegrationInfo: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = Icons.Rounded.Autorenew,
      contentDescription =
        stringResource(R.string.tasker_integration_field_icon),
    )
    Column(
      modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
      horizontalAlignment = Alignment.Start,
    ) {
      Text(
        text = stringResource(R.string.tasker_integration_field_label),
        style = MaterialTheme.typography.titleLarge,
      )
      Text(
        text = stringResource(R.string.tasker_integration_field_desc),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Button(onClick = onShowTaskerIntegrationInfo) {
        Text(text = stringResource(R.string.tasker_integration_info_button))
      }
    }
    Switch(
      checked = isTaskerIntegrationEnabled,
      onCheckedChange = onTaskerIntegrationChange,
    )
  }
}
