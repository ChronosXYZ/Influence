/*
 *    Copyright 2019 ChronosX88
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.chronosx88.influence.views

import android.content.Context
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import io.github.chronosx88.influence.R

object ViewUtils {
    fun setupEditTextDialog(context: Context, message: String): Pair<AlertDialog.Builder, EditText> {
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setTitle(message)

        val input = EditText(context)
        val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        input.setSingleLine()
        input.layoutParams = lp

        alertDialog.setView(input)

        return Pair(alertDialog, input)
    }
}