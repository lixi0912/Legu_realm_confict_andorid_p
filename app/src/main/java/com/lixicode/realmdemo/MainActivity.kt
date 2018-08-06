package com.lixicode.realmdemo

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmConfiguration
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    lateinit var query: RealmResults<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        Realm.init(this)
        val config = RealmConfiguration.Builder()
                .schemaVersion(0)
                .name("test")
                .build()
        Realm.setDefaultConfiguration(config)


        query = Realm.getDefaultInstance()
                .where(User::class.java)
                .findAllAsync()
        query.addChangeListener(RealmChangeListener<RealmResults<User>> {
            val user = it.first(null)
            val loginStr = when {
                null == user -> "no user login here"
                !user.isValid -> "database was invalid"
                else -> "user login state:${user.login}"
            }
            titleText.text = loginStr
        })

        fab.setOnClickListener {
            tryLogin(it)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }


    var disposable: Disposable? = null
    private fun tryLogin(view: View) {
        disposable?.dispose()
        Observable
                .defer {
                    Observable.just("1")
                }
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map {
                    Realm.getDefaultInstance().use {
                        it.executeTransaction {
                            val user = it.where(User::class.java)
                                    .findFirst()
                            if (null != user) {
                                user.login = !user.login
                            } else {
                                val userObj = User()
                                userObj.login = true
                                it.copyToRealm(userObj)
                            }
                        }
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Snackbar.make(view, "切换登录成功", Snackbar.LENGTH_SHORT).show()
                }, {
                    Snackbar.make(view, it.message!!, Snackbar.LENGTH_SHORT).show()
                    it.printStackTrace()
                })


    }
}
