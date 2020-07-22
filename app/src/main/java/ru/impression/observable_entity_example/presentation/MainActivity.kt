package ru.impression.observable_entity_example.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.impression.observable_entity_example.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager.findFragmentByTag(MainFragmentComponent::class.simpleName)
            ?: supportFragmentManager.beginTransaction().replace(
                R.id.container,
                MainFragmentComponent(),
                MainFragmentComponent::class.simpleName
            ).commit()
    }
}
