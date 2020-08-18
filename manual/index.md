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

## Exchange tab
![cryptonose exchange tab]({{ site.baseurl }}/images/cryptonose-exchange.png "Main window, exchange tab")
* ***Tab header***. On the tab header, near the exchange name, you can see a square indicating connection status. Red - disconnected, yellow - connecting, green - connected.
* **Table** - Visible table may increase resources (CPU) usage. Column names are self-explanatory, with one exception. There was not enough place for the word "change" in relative change columns ;) You can open web browser with market web page by double-clicking on a row.
* ***Log*** - (disabled by default) describes what is happening. You can enable log by clicking ***Show log***.
* ***Sound, Browser*** and ***Notification*** - same as is _Global options_ but only for given exchange.


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

## Notification

![cryptonose notification]({{ site.baseurl }}/images/notification.png "cryptonose desktop notification")
You can **click on the notification to run web browser**.
Notification shows details of price alert: percent change, relative change, time period, real change period, final price.
