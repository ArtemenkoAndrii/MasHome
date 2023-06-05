package com.mas.mobile

import android.app.Application
import android.content.Context
import com.mas.mobile.domain.budget.BudgetRepository
import com.mas.mobile.domain.budget.ExpenditureRepository
import com.mas.mobile.domain.budget.SpendingRepository
import com.mas.mobile.domain.message.MessageRepository
import com.mas.mobile.domain.message.MessageRuleRepository
import com.mas.mobile.domain.settings.SettingsRepository
import com.mas.mobile.presentation.activity.MainActivity
import com.mas.mobile.presentation.activity.PolicyActivity
import com.mas.mobile.presentation.activity.fragment.CommonFragment
import com.mas.mobile.presentation.viewmodel.*
import com.mas.mobile.repository.BudgetRepositoryImpl
import com.mas.mobile.repository.budget.ExpenditureRepositoryImpl
import com.mas.mobile.repository.budget.SpendingRepositoryImpl
import com.mas.mobile.repository.db.config.AppDatabase
import com.mas.mobile.repository.message.MessageRepositoryImpl
import com.mas.mobile.repository.message.MessageRuleRepositoryImpl
import com.mas.mobile.repository.settings.SettingsRepositoryImpl
import com.mas.mobile.service.DateListener
import com.mas.mobile.service.NotificationListener
import com.mas.mobile.service.SmsListener
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

class MasApplication: Application() {
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent
            .builder()
            .context(this)
            .build()
    }
}

val Context.appComponent: AppComponent
    get() = when(this) {
        is MasApplication -> appComponent
        else -> this.applicationContext.appComponent
    }

@Component(modules = [AppModule::class])
@Singleton
interface AppComponent {
    fun messageListViewModel(): MessageListViewModel.Factory

    fun messageRuleViewModel(): MessageRuleViewModel.Factory
    fun messageRuleListViewModel(): MessageRuleListViewModel.Factory

    fun budgetViewModel(): BudgetViewModel.Factory
    fun budgetListViewModel(): BudgetListViewModel.Factory

    fun expenditureViewModel(): ExpenditureViewModel.Factory
    fun expenditureListViewModel(): ExpenditureListViewModel.Factory

    fun spendingViewModel(): SpendingViewModel.Factory
    fun spendingListViewModel(): SpendingListViewModel.Factory

    fun settingsModel(): SettingsViewModel.Factory

    fun injectSmsListener(smsListener: SmsListener)
    fun injectDateListener(dateListener: DateListener)
    fun injectNotificationListener(notificationListener: NotificationListener)
    fun injectCommonFragment(wrapper: CommonFragment.CommonFragmentWrapper)

    fun injectMainActivity(mainActivity: MainActivity)
    fun injectPolicyActivity(policyActivity: PolicyActivity)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun context(context: Context): Builder
        fun build(): AppComponent
    }
}

@Module
class AppModule {
    @Provides
    @Singleton
    fun providesDb(context: Context): AppDatabase = AppDatabase.getInstance(context)

    @Provides
    @Singleton
    fun resolveBudgetRepository(db: AppDatabase): BudgetRepository {
        return BudgetRepositoryImpl(db)
    }

    @Provides
    @Singleton
    fun resolveExpenditureRepository(db: AppDatabase): ExpenditureRepository {
        return ExpenditureRepositoryImpl(db)
    }

    @Provides
    @Singleton
    fun resolveSpendingRepository(db: AppDatabase): SpendingRepository {
        return SpendingRepositoryImpl(db)
    }

    @Provides
    @Singleton
    fun resolveMessageRepository(db: AppDatabase): MessageRepository {
        return MessageRepositoryImpl(db)
    }

    @Provides
    @Singleton
    fun resolveMessageRuleRepository(db: AppDatabase): MessageRuleRepository {
        return MessageRuleRepositoryImpl(db)
    }

    @Provides
    @Singleton
    fun resolveSettingsRepository(db: AppDatabase): SettingsRepository {
        return SettingsRepositoryImpl(db)
    }
}