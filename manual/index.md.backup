{% include menu.md %}

# Manual

I've tried to make Cryptonose easy to use, but in case I've failed, here is the manual ;)

## First run
After first run, you will see main application window with price alerts tab. No alerts will occur unless you start any of supported exchanges. To do so, click on _Add exchange..._ and choose an exchange.

Cryptonose needs to know on which currency pairs it should track price changes. This is not configured so far so you will be asked to do it. There are two sections in pairs selection window:

* ***Choose markets***. Most of exchanges provide markets with more than one quote currency. Example of BTC markets (BTC as quote currency): ETH/BTC, LTC/BTC, XMR/BTC. You can use this option to track all pairs with significant volume. Select one one or more markets, then click on _Min 24h volume_ table cell to set volume and press Enter co confirm.
* ***Select additional pairs***. Manually choose currency pairs. You can use only this option or combine it with _Choose markets_ filter.

![cryptonose paris configuration]({{ site.baseurl }}/images/pairs-settings.png "Pairs configuration window")

Note that choosing a lot of currency pairs will cause longer initialization (Cryptonose needs to get chart data from exchange). Bitfinex has quite strict limits for downloading chart data - getting data for one currency pair will take at least 18 seconds.  
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
![cryptonose price alerts tab]({{ site.baseurl }}/images/cryptonose-price-alerts.png "Main window, price alerts")
### What is relative price change?
Relative price change is an indicator reflecting price change in given period in comparison to the average price change in the same time periods in past. The "past" is set to 50 periods.
To visualize, example values of relative price change:
* around 1 is typical value
* 4 is pretty much - means that probably something interesting is happening with the price
* 8 is very much - almost surely something interesting is happening with the price

### Price alert
![cryptonose price alert]({{ site.baseurl }}/images/cryptonose-price-alert.png "Price alert node")
* ***Price change*** - percent change in the price in given period. In parentheses - relative price change.
* ***Chart*** - minimalistic graph showing short (2 hours) price history. Just to illustrate recent trend.
* ***Pair*** - currency pair name in format _base/quote_. The color is different for every exchange.
* ***Final price***. Example: if price alert is triggered by price drop from 0.15 to 0.14, the final price is 0.14
* ***Period***. In parentheses - the real duration of price change in seconds.
* ***More*** - you can see buttons for ["Coins plugins"](#coins-plugins). They provide additional data about given coin.

## Exchange tab
![cryptonose exchange tab]({{ site.baseurl }}/images/cryptonose-exchange.png "Main window, exchange tab")

* ***Tab header***. On the tab header, near the exchange name, you can see a square indicating connection status. Red - disconnected, yellow - connecting, green - connected.
* **Table** - Visible table may increase resources (CPU) usage. Column names are self-explanatory, with one exception. There was not enough place for the word "change" in relative change columns ;) You can open web browser with market web page by double-clicking on a row.
* ***Log*** - (disabled by default) describes what is happening. You can enable log by clicking ***Show log***.
* ***Sound, Browser*** and ***Notification*** - same as is _Global options_ but only for given exchange.

## Search for a currency pair
A button to show search interface (_Find (press F)..._) is always visible at the top of the Cryptonose window. You can click on it or just press F key to start typing, to find desired currency pair. It's designed to let you access exchange page quickly - just press enter to go to the page for the top pair in the search results or use arrow keys to select another pair before pressing enter. You can also do use a double-click.

When you've finished your search, press ESC or click on (_Find (press F)..._) button to hide the interface. Also it will be hidden automatically if you start interacting with the rest of the Cryptonose interface. 

## Coins plugins
You can find small buttons with colored text on the price alerts tab and currency pairs tables. Currently there are 2 buttons - plugins:

* **CoinGecko plugin (_CG_)** - show additional data about coin's price, market cap and usually a short description. All the data are from CoinGecko website. The data are downloaded using coin's symbol (BTC, ETH, DOGE etc.). If there's a situation when there are multiple coins with the same symbol (which is not very likely), data for a coin with highest market cap will be shown. So keep in mind there's a minimal chance that data for wrong coin will be shown. 
* **CryptoPanic plugin (_CG_)** - show latest news about a coin from CryptoPanic website. Precisely, news for all coins with given symbol are shown in rare cases when multiple coins use this symbol.

## Alerts conditions
The default values should be useful. You may want to try them before considering any changes.
These settings could be saved for only selected exchange or for all exchanges.

Price alert is triggered by:

* significant percent **and** relative price changes (_Required..._ fields)
* significant relative change only (_Sufficient..._ fields).

#### Example

![cryptonose alert threshold]({{ site.baseurl }}/images/cryptonose-thresholds.png "5m period price alert thresholds")

* Price rise 3.5%, relative 4.5 will trigger an alert.
* Price drop -3.5%, relative -4.5 will trigger an alert.
* Price rise 3.5%, relative 3 will not trigger an alert - relative change is too low.
* Price rise 1%, relative 9.3 will trigger the alert - relative change is bigger than _sufficient_ value.

#### Cryptonose liquidity factor
Cryptonose liquidity factor is a very simple indicator that could help filter out alerts on currency pairs that are not liquid (and because of that - usually volatile in undesirable way). For given period of time (5m, 30m) liquidity factor is:

(number-of-periods-where-price-changed) / (number-of-periods)

Number of periods is 50, same as for calculating [relative change](#what-is-relative-price-change). 

To sum up, low value of liquidity factor for given currency pair means that there were recently many periods when price didn't change.

## Notification

![cryptonose notification]({{ site.baseurl }}/images/notification.png "cryptonose desktop notification")

You can **click on the notification to run web browser**.
Notification shows details of price alert: percent change, relative change, time period, real change period, final price.
