package com.mas.mobile

import android.app.Application
import android.content.Context
import com.mas.mobile.domain.budget.BudgetRepository
import com.mas.mobile.domain.budget.ExchangeRepository
import com.mas.mobile.domain.budget.ExpenditureRepository
import com.mas.mobile.domain.budget.SpendingRepository
import com.mas.mobile.domain.message.MessageAnalyzer
import com.mas.mobile.domain.message.MessageRepository
import com.mas.mobile.domain.message.MessageRuleRepository
import com.mas.mobile.domain.message.MessageTemplateRepository
import com.mas.mobile.domain.message.QualifierRepository
import com.mas.mobile.domain.settings.DeferrableActionRepository
import com.mas.mobile.domain.settings.SettingsRepository
import com.mas.mobile.presentation.activity.MainActivity
import com.mas.mobile.presentation.activity.PolicyActivity
import com.mas.mobile.presentation.activity.fragment.CommonFragment
import com.mas.mobile.presentation.viewmodel.*
import com.mas.mobile.repository.BudgetRepositoryImpl
import com.mas.mobile.repository.budget.ExpenditureRepositoryImpl
import com.mas.mobile.repository.budget.FreeCurrencyAPIRepositoryImpl
import com.mas.mobile.repository.budget.SpendingRepositoryImpl
import com.mas.mobile.repository.db.config.AppDatabase
import com.mas.mobile.repository.message.MessageRepositoryImpl
import com.mas.mobile.repository.message.MessageRuleRepositoryImpl
import com.mas.mobile.repository.message.MessageTemplateRepositoryImpl
import com.mas.mobile.repository.message.QualifierRepositoryImpl
import com.mas.mobile.repository.settings.DeferrableActionRepositoryImpl
import com.mas.mobile.repository.settings.SettingsRepositoryImpl
import com.mas.mobile.service.*
import com.mas.mobile.service.ai.GPTMessageAnalyzer
import com.mas.mobile.service.ai.GptChatApiClient
import com.mas.mobile.service.ai.GptChatConnector
import com.mas.mobile.util.Analytics
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

    companion object {
        const val HOME_PAGE = "https://github.com/ArtemenkoAndrii/MasHome/wiki/What-Mas-Money-is%3F"
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
    fun messageTemplateListViewModel(): MessageTemplateListViewModel.Factory
    fun messageTemplateViewModel(): MessageTemplateViewModel.Factory
    fun messageRuleViewModel(): MessageRuleViewModel.Factory
    fun messageRuleListViewModel(): MessageRuleListViewModel.Factory

    fun budgetViewModel(): BudgetViewModel.Factory
    fun budgetListViewModel(): BudgetListViewModel.Factory

    fun expenditureViewModel(): ExpenditureViewModel.Factory
    fun expenditureListViewModel(): ExpenditureListViewModel.Factory

    fun spendingViewModel(): SpendingViewModel.Factory
    fun spendingListViewModel(): SpendingListViewModel.Factory

    fun qualifierListViewModel(): QualifierListViewModel.Factory

    fun settingsModel(): SettingsViewModel.Factory

    fun chartViewModelModel(): ChartViewModel.Factory

    fun injectSmsListener(smsListener: SmsListener)
    fun injectDateListener(dateListener: DateListener)
    fun injectNotificationListener(notificationListener: NotificationListener)
    fun injectCommonFragment(wrapper: CommonFragment.CommonFragmentWrapper)
    fun injectAppUpdateCheckWorker(worker: AppUpdateCheckWorker)

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
    fun resolveMessageTemplateRepository(db: AppDatabase): MessageTemplateRepository {
        return MessageTemplateRepositoryImpl(db)
    }

    @Provides
    @Singleton
    fun resolveSettingsRepository(db: AppDatabase): SettingsRepository {
        return SettingsRepositoryImpl(db)
    }

    @Provides
    @Singleton
    fun resolveQualifierRepository(db: AppDatabase): QualifierRepository {
        return QualifierRepositoryImpl(db)
    }

    @Provides
    @Singleton
    fun resolveExchangeRepository(): ExchangeRepository {
        return FreeCurrencyAPIRepositoryImpl()
    }

    @Provides
    @Singleton
    fun resolveDeferredActionRepositoryImpl(db: AppDatabase): DeferrableActionRepository {
        return DeferrableActionRepositoryImpl(db)
    }

    @Provides
    @Singleton
    fun resolveGptChatService(): GptChatConnector =
        GptChatApiClient().gptChatConnector

    @Provides
    @Singleton
    fun resolveMessageProcessor(gptChatConnector: GptChatConnector): MessageAnalyzer =
        GPTMessageAnalyzer(gptChatConnector)

    @Provides
    @Singleton
    fun resolveTaskService(coroutineService: CoroutineService): TaskService {
        return coroutineService
    }

    @Provides
    @Singleton
    fun resolveAnalytics(): Analytics {
        return Analytics()
    }

    @Provides
    @Singleton
    fun resolveNotificationService(context: Context, resourceService: ResourceService): NotificationService {
        return NotificationService(context, resourceService)
    }
}