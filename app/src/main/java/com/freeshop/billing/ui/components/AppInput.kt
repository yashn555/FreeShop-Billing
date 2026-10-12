package com.freeshop.billing.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.freeshop.billing.ui.theme.*

@Composable
fun AppInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailingIcon: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false,
    isError: Boolean = false,
) {
    OutlinedTextField(
        value          = value,
        onValueChange  = onValueChange,
        label          = { Text(label) },
        placeholder    = if (placeholder.isNotEmpty()) { { Text(placeholder) } } else null,
        modifier       = modifier.fillMaxWidth(),
        singleLine     = singleLine,
        readOnly       = readOnly,
        isError        = isError,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        trailingIcon   = trailingIcon,
        shape          = RoundedCornerShape(10.dp),
        colors         = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = AccentNavy,
            unfocusedBorderColor = LineBlue,
            focusedLabelColor    = AccentNavy,
        ),
    )
}
