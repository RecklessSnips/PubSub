# Workflow
 
This News PubSub project consists 5 repository with 4 microservices + 1 library:

1. NewsManager: Responsible for extract News from the NewsAPI.org or NewsData.io periodically, and broadcast the News to the Solace Event Broker (Direct Publish). Right now only support for BBC, CNN, Reuters, NY Times, and Economist.
2. NewsSubscriber: Responsible for listening to all the News and save them to display on the landing page (just for fun but not really useful).
3. Notifications: The core microservice, responsible for subscriptions and broadcast the news to the client. The logic is that: always listen to the Broker, and whenever a News object is received, check it if the source of hte News is bing subscribed, if not, filter it out, if yes, publish to the Broker's Message Queue.
4. SEMP: This is mainly for the Header component in the frontend, to display the unread messages in the Bell dropdown (as shown in the video).
5. Utils: This is the library, it provides the News structure, error, API Response, and most importantly, the Connector class. This class has the essence, simplest way how to use Solace Event Broker's API.

## Workflow
![](/Users/ahsoka/Desktop/NewsPubSub Workflow.png)
Note: Checkout comments in the code!!!

## Video

https://drive.google.com/file/d/1MtZMuUqARbkJe2uo9zDpwUd70Ymzu8QC/view?usp=sharing

## Reference:
* Solace Event Broker: https://docs.solace.com/Get-Started/get-started-lp.htm
* Solace Event Broker code samples Java: https://github.com/SolaceSamples/solace-samples-java
* Solace Event Broker code samples JavaScript: https://github.com/SolaceSamples/solace-samples-javascript
* NewsData.io: https://newsdata.io/documentation#latest-news
* NewsApi.org: https://newsapi.org/docs/endpoints/everything