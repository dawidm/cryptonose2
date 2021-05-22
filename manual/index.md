{% include menu.md %}

# Manual

I've tried to make Cryptonose easy to use, but in case I've failed, here is the manual ;)

## First run
After first run, you will see main application window with price alerts tab. No alerts will occur unless you start any of supported exchanges. To do so, click on _Add exchange..._ and choose an exchange.

Cryptonose needs to know on which currency pairs it should track price changes. This is not configured so far so you will be asked to do it. There are two sections in pairs selection window:

* ***Choose markets***. Most of the exchanges provide markets with more than one quote currency. Example of BTC markets (BTC as quote currency): ETH/BTC, LTC/BTC, XMR/BTC. You can use this option to track all pairs with significant volume. Select one one or more markets, then click on _Min 24h volume_ table cell to set volume and press Enter co confirm.
* ***Select additional pairs***. Manually choose currency pairs. You can use only this option or combine it with _Choose markets_ filter.

![cryptonose paris configuration]({{ site.baseurl }}/images/pairs-settings.png "Pairs configuration window")

**Note that choosing a lot of currency pairs will cause longer initialization** (Cryptonose needs to get chart data from the exchange). Bitfinex has quite strict limits for downloading chart data - getting data for one currency pair will take at least 18 seconds.  
Very low volume (so with low liquidity) currency pairs could trigger a lot of undesirable price alerts. You should choose currency pairs carefully.

## Main window
* **_Find (press F)..._** lets you make a quick search for currency pair from any connected exchange, to see information about it or access it's exchange web page (you can do it quickly just by pressing the enter key).
* **_Price alerts_ tab** shows recent price alerts from all exchanges.
* **Exchange tabs** - every exchange runs in separate tab. Near the exchange name there's a little box showing connection status: red - disconnected, green - connected, yellow - reconnecting.
* ***Global options*** - preferences for all exchanges:
  * _Sound_ - enable sound when price alert occurs
  * _Browser_ - run web browser with adequate market on exchange website
  * _Notification_ - show notification with alert details
  * _Power Save_ - disable tables in exchange tabs - saving CPU and battery, especially when prices are very volatile.

## Price alerts tab
[![cryptonose price alerts tab]({{ site.baseurl }}/images/cryptonose-price-alerts.png "Main window, price alerts")]({{ site.baseurl }}/images/cryptonose-price-alerts.png)
### What is relative price change?
Relative price change is an indicator reflecting price change in given period in comparison to the average price change in the same time periods in past. The "past" is set to 50 periods. More technically this average is a median of absolute differences between high and low of past periods (candles).
To visualize, example values of relative price change:
* around 1 is typical value
* 4 is pretty much - means that probably something interesting is happening with the price
* 8 is very much - almost surely something interesting is happening with the price

### Price alert
[![cryptonose price alert]({{ site.baseurl }}/images/cryptonose-price-alert.png "Price alert node")]({{ site.baseurl }}/images/cryptonose-price-alert.png)

* ***Price change*** - percent change in the price in given period. In parentheses - relative price change.
* ***Chart*** - minimalistic graph showing short (2 hours) price history. Just to illustrate a recent trend.
* ***Pair*** - currency pair name in format _base/quote_. The color is specific for the exchange. Next to a name you can see button for blocking alerts described in the next paragraph.
* ***Final price***. Example: if price alert is triggered by price drop from 0.15 to 0.14, the final price is 0.14
* ***Period***. In parentheses - the real duration of price change in seconds.
* ***More*** - you can see buttons for ["Coins plugins"](#coins-plugins). They provide additional data about given coin.

### Blocking alerts

You can block subsequent alerts for currency pairs using a block button that's next to the pair name in the alerts tab. Blocked means that you will not see or hear any price alert on given currency pair. Blocks for specified time (1 hour, 2 hours etc.) will not persist if you close Cryptonose and run it again, but block for an unspecified time will.

You can see and remove alert blocks using clicking on _Manage blocks_ button. You can also do the same using _Alert blocks..._ button in the exchange tab.   

## Exchange tab
[![cryptonose exchange tab]({{ site.baseurl }}/images/cryptonose-exchange.png "Main window, exchange tab")]({{ site.baseurl }}/images/cryptonose-exchange.png)

* ***Tab header***. On the tab header, near the exchange name, you can see a square indicating connection status. Red - disconnected, yellow - connecting, green - connected.
* **Table** - Visible table may increase resources (CPU) usage. Column names are self-explanatory, with one exception. There was not enough place for the word "change" in relative change columns ;) You can open web browser with market web page by double-clicking on a row.
* ***Log*** - (disabled by default) describes what is happening. You can enable log by clicking ***Show log***.
* ***Sound, Browser*** and ***Notification*** - same as is _Global options_ but only for given exchange.

### What exactly are price changes in Cryptonose?
Price change in Cryptonose is based on maximum (positive or negative) change during recent time period (5 minutes or 30 minutes). You should not confuse it with a change between beginning and ending of the period. For example if price 5 minutes ago was 2.0, then it went up to 4.0 and now it dropped slightly to 3.7, 5m price change values will be calculated for biggest rise - from 2.0 to 4.0 - in percents it's +100%.

## Search for a currency pair

[![cryptonose search]({{ site.baseurl }}/images/cryptonose-search.png "Cryptonose search interface")]({{ site.baseurl }}/images/cryptonose-search.png)

A button to show search interface (_Find (press F)..._) is always visible at the top of the Cryptonose window. You can click on it or just press F key to start typing, to find desired currency pair. It's designed to let you access exchange page quickly - just press enter to go to the page for the top pair in the search results or use arrow keys to select another pair before pressing enter. You can also do use a double-click.

When you've finished your search, press ESC or click on (_Find (press F)..._) button to hide the interface. Also, it will be hidden automatically if you start interacting with the rest of the Cryptonose interface. 

## Pinned tickers

In currency pairs search table you can see a _Pin_ option. If you pin currency pair, you will see a small box with real-time price and mini-chart - that's a _pinned ticker_. Tickers will appear in the same order as their currency pairs were pinned. Pinned currency pairs will also be placed at the top in search table.

There's a limit of a 10 visible pinned tickers. If you pin more - the most recent ones just won't be shown.

## Coins plugins
You can find small buttons with colored text on the price alerts tab and currency pairs tables. Currently, there are 2 buttons - plugins:

* **CoinGecko plugin (_CG_)** - show additional data about coin's price, market cap and usually a short description. All the data are from CoinGecko website. The data are downloaded using coin's symbol (BTC, ETH, DOGE etc.). If there's a situation when there are multiple coins with the same symbol (which is not very likely), data for a coin with highest market cap will be shown. So keep in mind there's a minimal chance that data for wrong coin will be shown. 
* **CryptoPanic plugin (_CG_)** - show latest news about a coin from CryptoPanic website. Precisely, news for all coins with given symbol are shown in rare cases when multiple coins use this symbol.

![coingecko plugin]({{ site.baseurl }}/images/cryptonose-coingecko.jpg "CoinGecko plugin")

![cryptopanic plugin]({{ site.baseurl }}/images/cryptonose-cryptopanic.jpg "CryptoPanic plugin")

## Alerts conditions
The predefined values should be useful. You may want to try them before considering any changes. Try starting with the _Low_ settings and then switching to the next presets if the alerts are to frequent.

Price alert is triggered by:

* significant percent **and** relative price changes (_Required..._ fields)
* significant relative change only (_Sufficient..._ fields).

#### Example

![cryptonose alert threshold]({{ site.baseurl }}/images/cryptonose-thresholds.png "5m period price alert thresholds")

* Price rise 3.1%, relative 4.1 will trigger an alert.
* Price drop -3.1%, relative -4.1 will trigger an alert.
* Price rise 3.1%, relative 2.0 will not trigger an alert - relative change is too low.
* Price rise 1.0%, relative 9.3 will trigger the alert - relative change is bigger than _sufficient_ value.

These settings are quite flexible. If you're only interested in percent changes, you can set the required relative change to 0, and sufficient relative change to a highest possible value, which is 100.

#### Alerts logic settings

* ***After an alert block subsequent alerts for the same pair for 30 minutes***. You can use this to make alerts less frequent, but keep in mind that during this time something interesting could happen, and you won't be notified. 
* ***Allow a subsequent alert if the price change value is more than 2x higher than in the previous one***. For example if you get alert for a significant price rise and after a feq seconds the price rises even more, more than 2x than the previous change - you'll get a second alert.

#### Cryptonose liquidity factor
Cryptonose liquidity factor is a very simple indicator that could help filter out alerts on currency pairs that are not liquid (and because of that - usually volatile in undesirable way). For given period (5m, 30m) liquidity factor is:

(number-of-periods-where-price-changed) / (number-of-periods)

Number of periods is 50, same as for calculating [relative change](#what-is-relative-price-change). 

To sum up, low value of liquidity factor for given currency pair means that there were recently many periods when price didn't change.

## Notification

![cryptonose notification]({{ site.baseurl }}/images/notification.png "cryptonose desktop notification")

You can **click on the notification to run web browser**.
Notification shows details of price alert: percent change, relative change, time period, real change period, final price.
