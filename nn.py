import tensorflow as tf
import prettytensor as pt 
from tensorflow.examples.tutorials.mnist import input_data
from tensorflow.python.framework.graph_util import convert_variables_to_constants

data = input_data.read_data_sets('data/MNIST/', one_hot=True)

x_image = tf.placeholder(tf.float32, shape=[None, 28 * 28], name='x')

y_true = tf.placeholder(tf.float32, shape=[None, 10], name='y_true')

y_true_cls = tf.argmax(y_true, dimension=1)

x_pretty = pt.wrap(x_image)

with pt.defaults_scope(activation_fn=tf.nn.tanh):
	y_pred, loss = x_pretty.\
	fully_connected(size=1600, name='layer_fc0').\
	softmax_classifier(class_count=10, labels=y_true)

optimizer = tf.train.AdamOptimizer(learning_rate=1e-4).minimize(loss)

y_pred_cls = tf.argmax(y_pred, dimension=1)

output = tf.identity(y_pred, name="out")

correct_prediction = tf.equal(y_pred_cls, y_true_cls)

accuracy = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))

session = tf.Session()

session.run(tf.initialize_all_variables())

saver = tf.train.Saver()

train_batch_size = 128

def optimize(num_iterations):
    for i in range(0, num_iterations):

        x_batch, y_true_batch = data.train.next_batch(train_batch_size)

        feed_dict_train = {x_image: x_batch, y_true: y_true_batch}

        session.run(optimizer, feed_dict=feed_dict_train)

        if i % 100 == 0:
            acc = session.run(accuracy, feed_dict=feed_dict_train)
            msg = "Iteration: {0:>6}, Accuracy: {1:>6.1%}"
            print(msg.format(i + 1, acc))

optimize(1200)
minimal_graph = convert_variables_to_constants(session, session.graph_def, ["out"])
tf.train.write_graph(minimal_graph, "train", 'mnist_model.pb', as_text=False)
tf.train.write_graph(minimal_graph, "train", 'mnist_model.txt', as_text=True)