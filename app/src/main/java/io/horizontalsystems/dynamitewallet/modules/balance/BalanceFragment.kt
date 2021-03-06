package io.horizontalsystems.dynamitewallet.modules.balance

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.appbar.AppBarLayout
import io.horizontalsystems.dynamitewallet.R
import io.horizontalsystems.dynamitewallet.core.AdapterState
import io.horizontalsystems.dynamitewallet.core.App
import io.horizontalsystems.dynamitewallet.core.setOnSingleClickListener
import io.horizontalsystems.dynamitewallet.lib.BalanceSortDialogFragment
import io.horizontalsystems.dynamitewallet.modules.main.MainActivity
import io.horizontalsystems.dynamitewallet.modules.managecoins.ManageCoinsModule
import io.horizontalsystems.dynamitewallet.ui.extensions.NpaLinearLayoutManager
import io.horizontalsystems.dynamitewallet.viewHelpers.AnimationHelper
import io.horizontalsystems.dynamitewallet.viewHelpers.DateHelper
import io.horizontalsystems.dynamitewallet.viewHelpers.LayoutHelper
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_balance.*
import kotlinx.android.synthetic.main.view_holder_add_coin.*
import kotlinx.android.synthetic.main.view_holder_coin.*
import kotlinx.android.synthetic.main.view_holder_coin.coinIcon
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigDecimal


class BalanceFragment : Fragment(), CoinsAdapter.Listener, BalanceSortDialogFragment.Listener {

    private lateinit var viewModel: BalanceViewModel
    private var coinsAdapter = CoinsAdapter(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_balance, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        toolbarTitle.setText(R.string.Balance_Title)
        viewModel = ViewModelProviders.of(this).get(BalanceViewModel::class.java)
        viewModel.init()

        viewModel.openReceiveDialog.observe(viewLifecycleOwner, Observer { coinCode ->
            coinCode?.let {
                (activity as? MainActivity)?.openReceiveDialog(it)
            }
        })

        viewModel.openSendDialog.observe(viewLifecycleOwner, Observer { coinCode ->
            coinCode?.let {
                (activity as? MainActivity)?.openSendDialog(it)
            }
        })

        viewModel.balanceColorLiveDate.observe(viewLifecycleOwner, Observer { color ->
            color?.let { colorRes ->
                context?.let { it ->
                    ballanceText.setTextColor(ContextCompat.getColor(it, colorRes))
                }
            }
        })

        viewModel.didRefreshLiveEvent.observe(viewLifecycleOwner, Observer {
            pullToRefresh.isRefreshing = false
        })

        viewModel.openManageCoinsLiveEvent.observe(viewLifecycleOwner, Observer {
            context?.let { context -> ManageCoinsModule.start(context) }
        })

        viewModel.reloadLiveEvent.observe(viewLifecycleOwner, Observer {
            coinsAdapter.notifyDataSetChanged()
            reloadHeader()
            if (viewModel.delegate.itemsCount > 0) {
                shimmerViewWrapper.stopShimmer()
                shimmerViewWrapper.animate().alpha(0f)
                recyclerCoins.animate().alpha(1f)
            }
        })

        viewModel.enabledCoinsCountLiveEvent.observe(viewLifecycleOwner, Observer { size ->
            size?.let {
                if (it > 0 && viewModel.delegate.itemsCount == 0) {
                    setPlaceholders(it)

                    recyclerCoins.alpha = 0f
                    shimmerViewWrapper.alpha = 1f
                    shimmerViewWrapper.startShimmer()
                } else if (it == 0) {
                    recyclerCoins.alpha = 1f
                    shimmerViewWrapper.alpha = 0f
                }
            }
        })

        viewModel.reloadHeaderLiveEvent.observe(viewLifecycleOwner, Observer {
            reloadHeader()
        })

        viewModel.reloadItemLiveEvent.observe(viewLifecycleOwner, Observer { position ->
            position?.let {
                coinsAdapter.notifyItemChanged(it)
            }
        })

        viewModel.openSortingTypeDialogLiveEvent.observe(viewLifecycleOwner, Observer { sortingType ->
            sortingType?.let {
                BalanceSortDialogFragment
                        .newInstance(this, it)
                        .show(childFragmentManager, "select_sorting_type_alert")
            }
        })

        viewModel.setSortingOnLiveEvent.observe(viewLifecycleOwner, Observer { isOn ->
            isOn?.let { visible ->
                sortButton.visibility = if (visible) View.VISIBLE else View.GONE
            }
        })


        coinsAdapter.viewDelegate = viewModel.delegate
        recyclerCoins.adapter = coinsAdapter
        recyclerCoins.layoutManager = NpaLinearLayoutManager(context)
        (recyclerCoins.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false

        pullToRefresh.setOnRefreshListener {
            viewModel.delegate.refresh()
        }

        activity?.theme?.let { theme ->
            LayoutHelper.getAttr(R.attr.SwipeRefreshBackgroundColor, theme)?.let { color ->
                pullToRefresh.setProgressBackgroundColorSchemeColor(color)
            }
            LayoutHelper.getAttr(R.attr.SwipeRefreshSpinnerColor, theme)?.let { color ->
                pullToRefresh.setColorSchemeColors(color)
            }
        }

        sortButton.setOnClickListener {
            viewModel.delegate.onSortClick()
        }
        setAppBarAnimation()
    }

    override fun onSortItemSelect(sortType: BalanceSortType) {
        viewModel.delegate.onSortTypeChanged(sortType)
    }

    private fun setPlaceholders(count: Int) {
        placeholderContainer.removeAllViews()
        val placeholdersCount = Math.min(count, 6)
        for (i in 1..placeholdersCount) {
            val placeholder = LayoutInflater.from(context).inflate(R.layout.view_holder_coin_placeholder, placeholderContainer, false)
            placeholderContainer.addView(placeholder)
        }
        val placeholder = LayoutInflater.from(context).inflate(R.layout.add_coin_placeholder, placeholderContainer, false)
        placeholderContainer.addView(placeholder)
    }

    private fun setAppBarAnimation() {
        toolbarTitle.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                toolbarTitle.pivotX = 0f
                toolbarTitle.pivotY = toolbarTitle.height.toFloat()
                toolbarTitle.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        app_bar_layout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val fraction = Math.abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange
            var alphaFract = 1f - fraction
            if (alphaFract < 0.20) {
                alphaFract = 0f
            }
            toolbarTitle.alpha = alphaFract
            toolbarTitle.scaleX = (1f - fraction / 3)
            toolbarTitle.scaleY = (1f - fraction / 3)
        })
    }

    override fun onResume() {
        super.onResume()
        coinsAdapter.notifyDataSetChanged()
    }

    private fun reloadHeader() {
        val headerViewItem = viewModel.delegate.getHeaderViewItem()

        context?.let {
            val color = if (headerViewItem.upToDate) R.color.white else R.color.white
            ballanceText.setTextColor(ContextCompat.getColor(it, color))
        }

        ballanceText.text = headerViewItem.currencyValue?.let {
            io.horizontalsystems.dynamitewallet.core.App.numberFormatter.format(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerCoins.adapter = null
    }

    override fun onSendClicked(position: Int) {
        viewModel.onSendClicked(position)
    }

    override fun onReceiveClicked(position: Int) {
        viewModel.onReceiveClicked(position)
    }

    override fun onItemClick(position: Int) {
        coinsAdapter.toggleViewHolder(position)
    }

    override fun onAddCoinClick() {
        viewModel.delegate.openManageCoins()
    }
}

class CoinsAdapter(private val listener: Listener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface Listener {
        fun onSendClicked(position: Int)
        fun onReceiveClicked(position: Int)
        fun onItemClick(position: Int)
        fun onAddCoinClick()
    }

    private val coinType = 1
    private val addCoinType = 2

    private var expandedViewPosition: Int? = null

    lateinit var viewDelegate: BalanceModule.IViewDelegate

    fun toggleViewHolder(position: Int) {
        expandedViewPosition?.let {
            notifyItemChanged(it, false)
        }

        if (expandedViewPosition != position) {
            notifyItemChanged(position, true)
        }

        expandedViewPosition = if (expandedViewPosition == position) null else position
    }

    override fun getItemCount() = viewDelegate.itemsCount + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) addCoinType else coinType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            when (viewType) {
                addCoinType -> ViewHolderAddCoin(LayoutInflater.from(parent.context).inflate(R.layout.view_holder_add_coin, parent, false), listener)
                else -> ViewHolderCoin(LayoutInflater.from(parent.context).inflate(R.layout.view_holder_coin, parent, false), listener)
            }

    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {}

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (holder !is ViewHolderCoin) return

        if (payloads.isEmpty()) {
            holder.bind(viewDelegate.getViewItem(position), expandedViewPosition == position)
        } else if (payloads.any { it is Boolean }) {
            holder.bindPartial(expandedViewPosition == position)
        }
    }
}

class ViewHolderAddCoin(override val containerView: View, listener: CoinsAdapter.Listener) : RecyclerView.ViewHolder(containerView), LayoutContainer {
    private var refreshDisposables: CompositeDisposable = CompositeDisposable()

    private fun getRetrofit(): Retrofit {
        refreshDisposables.clear()
        refreshDisposables.add(
        return Retrofit.Builder()
                .baseUrl("https://dyt.meany.xyz/data/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        )
    }

    private fun searchData(){
        doAsync {
            val call = getRetrofit().create(GetData::class.java).getDytStats("").execute()
            val dytStat = call.body() as GetDYT
            uiThread {
                textBurned.text= io.horizontalsystems.dynamitewallet.core.App.numberFormatter.format(dytStat.stat.get("burned").asDouble)
                textTransaction.text = io.horizontalsystems.dynamitewallet.core.App.numberFormatter.format(dytStat.stat.get("transactions").asDouble)
                textLast.text = io.horizontalsystems.dynamitewallet.core.App.numberFormatter.format(dytStat.stat.get("burnLast24H").asDouble)
                textCirculation.text = io.horizontalsystems.dynamitewallet.core.App.numberFormatter.format(dytStat.stat.get("circulation").asDouble)
                textTotal.text = io.horizontalsystems.dynamitewallet.core.App.numberFormatter.format(dytStat.stat.get("supply").asDouble)
            }
        }
    }

    init {
        /*manageCoins.setOnSingleClickListener {
            listener.onAddCoinClick()
        }*/
        /*Add titles for dyt stats tokens*/
        titleBurned.setText(R.string.titleBurned)
        titleCirculation.setText(R.string.titleCirculation)
        titleLast.setText(R.string.titleLast)
        titleTransaction.setText(R.string.titleTransaction)
        titleTotal.setText(R.string.titleTotal)
        titleStat.setText(R.string.titleStat)

        searchData()
    }
}

class ViewHolderCoin(override val containerView: View, private val listener: CoinsAdapter.Listener) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    private var syncing = false
    /*private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
                .baseUrl("https://dyt.meany.xyz/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }
    private fun searchData(){
        val call = getRetrofit().create(GetData::class.java).getCharacterByName("data").execute()
        val puppies = call.body() as GetDYT
    }*/
    @SuppressLint("ResourceAsColor")
    fun bind(balanceViewItem: BalanceViewItem, expanded: Boolean) {
        //searchData()
        syncing = false
        buttonPay.isEnabled = false
        imgSyncFailed.visibility = View.GONE
        iconProgress.visibility = View.GONE

        balanceViewItem.state.let { adapterState ->
            when (adapterState) {
                is AdapterState.Syncing -> {
                    syncing = true
                    iconProgress.visibility = View.VISIBLE
                    iconProgress.setProgress(adapterState.progress.toFloat())
                    adapterState.lastBlockDate?.let {
                        textSyncProgress.text = containerView.context.getString(R.string.Balance_SyncedUntil, DateHelper.formatDate(it, "MMM d.yyyy"))
                    }
                            ?: run { textSyncProgress.text = containerView.context.getString(R.string.Balance_Syncing) }
                }
                is AdapterState.Synced -> {
                    if (balanceViewItem.coinValue.value > BigDecimal.ZERO) {
                        buttonPay.isEnabled = true
                        val color =  R.color.white
                        buttonPay.setTextColor(ContextCompat.getColor(containerView.context, color))
                    }
                    coinIcon.visibility = View.VISIBLE
                }
                is AdapterState.NotSynced -> {
                    imgSyncFailed.visibility = View.VISIBLE
                    coinIcon.visibility = View.GONE
                }
            }
        }

        balanceViewItem.currencyValue?.let {
            textCurrencyAmount.text = io.horizontalsystems.dynamitewallet.core.App.numberFormatter.format(it, trimmable = true)
            textCurrencyAmount.visibility = /*if (it.value.compareTo(BigDecimal.ZERO) == 0) View.GONE else*/ View.VISIBLE
            textCurrencyAmount.alpha = if (!balanceViewItem.rateExpired && balanceViewItem.state is AdapterState.Synced) 1f else 0.5f
        } ?: run { textCurrencyAmount.visibility = View.GONE }

        textCoinAmount.text = io.horizontalsystems.dynamitewallet.core.App.numberFormatter.format(balanceViewItem.coinValue)
        textCoinAmount.alpha = if (balanceViewItem.state is AdapterState.Synced) 1f else 0.3f

        textSyncProgress.visibility = if (expanded && syncing) View.VISIBLE else View.GONE
        textExchangeRate.visibility = if (expanded && syncing) View.GONE else View.VISIBLE

        coinIcon.bind(balanceViewItem.coin)
        textCoinName.text = balanceViewItem.coin.title
        textExchangeRate.text = balanceViewItem.exchangeValue?.let { exchangeValue ->
            val rateString = io.horizontalsystems.dynamitewallet.core.App.numberFormatter.format(exchangeValue, trimmable = true, canUseLessSymbol = false)
            containerView.context.getString(R.string.Balance_RatePerCoin, rateString, "")
        } ?: ""
        textExchangeRate.setTextColor(ContextCompat.getColor(containerView.context, if (balanceViewItem.rateExpired) R.color.steel_40 else R.color.grey))



        buttonPay.setOnSingleClickListener {
            listener.onSendClicked(adapterPosition)
        }

        buttonReceive.setOnSingleClickListener {
            listener.onReceiveClicked(adapterPosition)
        }

        buttonsWrapper.visibility = if (expanded) View.VISIBLE else View.GONE
        containerView.setOnClickListener {
            listener.onItemClick(adapterPosition)
        }

    }

    fun bindPartial(expanded: Boolean) {
        textSyncProgress.visibility = if (expanded && syncing) View.VISIBLE else View.GONE
        textExchangeRate.visibility = if (expanded && syncing) View.GONE else View.VISIBLE
        if (expanded) {
            AnimationHelper.expand(buttonsWrapper)
        } else {
            AnimationHelper.collapse(buttonsWrapper)
        }

    }

}
