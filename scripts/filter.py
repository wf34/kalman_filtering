
import csv
import sys

import numpy as np
import numpy.linalg
import matplotlib.pyplot as plt
import utm

from vis import load_time_lat_lon, do_graph

class kalman_filter:

  def get_filtered_states(self):
    return self.filtered_states


  def generate_mu_0(self, start_x, start_y):
    mu_0 = np.zeros((6, 1))
    mu_0[0, 0] = start_x
    mu_0[3, 0] = start_y
    return mu_0

  def generate_sigma_0(self):
    sigma_0 = np.zeros((6, 6))
    sigma_0 += 0.01
    sigma_0 += np.eye(6) * 0.1 ** 2
    return sigma_0


  def __init__(self, start_measurement):
    start_time, start_x, start_y = start_measurement
    mu_0 = self.generate_mu_0(start_x, start_y)
    sigma_0 = self.generate_sigma_0()

    self.prev_mu = mu_0
    self.prev_sigma = sigma_0
    self.prev_time = start_time
    self.filtered_states = []

    # transition matrix template
    self.A_template = np.zeros((6, 6)) + np.eye(6)
    self.A_template[0, 2] = 0.5
    self.A_template[3, 5] = 0.5

    self.A_template[0, 1] = 1
    self.A_template[1, 2] = 1

    self.A_template[3, 4] = 1
    self.A_template[4, 5] = 1

    # transition noize
    r = 0.1 ** 2
    self.R = np.zeros((6, 6))
    for indices in [(2, 2), (5, 5)]:
      self.R[indices] = r
    # state to measurement matrix
    self.C = np.zeros((2, 6))
    self.C[0, 0] = 1
    self.C[1, 3] = 1
    self.C = np.matrix(self.C)

    # measurement noize
    q = 50
    Q = np.zeros((2, 2))
    for indices in [(0, 0), (1, 1)]:
      Q[indices] = q ** 2
    self.Q = np.matrix(Q)
    

  def get_transition_matrix(self, delta_t):
    A = self.A_template.copy()
    for indices in [(0, 1), (1, 2), (3, 4), (4, 5)]:
      A[indices] *= delta_t
    for indices in [(0, 2), (3, 5)]:
      A[indices] *= delta_t ** 2
    return A


  def predict(self, current_t):
    print('*** predict %0.2f' % current_t)
    print('dt %0.2f' % (current_t - self.prev_time))
    A_t = self.get_transition_matrix(current_t - self.prev_time)
    print('Transition matrix A_t: \n', A_t)
    self.current_mu_hat = np.dot(A_t, self.prev_mu)
    self.current_sigma_hat = \
      np.dot(np.dot(A_t, self.prev_sigma), A_t.transpose()) + self.R
    self.prev_time = current_t
    print('mu_hat:\n', self.current_mu_hat)
    print('sigma_hat:\n', self.current_sigma_hat)


  def correct(self, measurement):
    current_time, x, y = measurement
    print('*** correct %0.2f' % current_time)
    z = np.array([x, y])[:, np.newaxis]

    K_t = np.matrix(self.current_sigma_hat) * self.C.T
    interm = self.C * np.matrix(self.current_sigma_hat) * self.C.T + self.Q
    K_t = K_t * interm.I
    print('K_t:\n', K_t)
    
    current_mu = self.current_mu_hat + \
       np.dot(K_t, z - np.dot(self.C, self.current_mu_hat))
    current_sigma = np.dot(np.eye(6) - K_t * self.C, self.current_sigma_hat)

    print('mu:\n', current_mu)
    print('compare_pose z[{}, {}], x[{}, {}]'.format(
          x, y,
          current_mu[0, 0], current_mu[3, 0]))

    self.prev_mu = current_mu
    self.prev_sigma = current_sigma
    self.filtered_states.append([current_time,
                                 current_mu[0, 0],
                                 current_mu[3, 0]])


def preprocess_dataset(sequence):
  sequence = list(map(lambda x: [x[0] * 1e-3] + list(utm.from_latlon(x[1], x[2]))[:2],
                      sequence))
  _, start_lat, start_lon = sequence[0]
  sequence = list(map(lambda x: [x[0], x[1] - start_lat, x[2] - start_lon],
                      sequence))
  return sequence


def do_kalman_filtering(sequence):
  kf = kalman_filter(sequence[0])
  counter = 0
  for data_entry in sequence[1:]:
    kf.predict(data_entry[0])
    kf.correct(data_entry)
    if counter > 3: 
      pass #break
    counter += 1

  return kf.get_filtered_states()
    

def write_to_csv(output_filepath, sequence):
  pass


def main():
  np.set_printoptions(precision = 3)
  csv_filepath = sys.argv[1]
  poses_sequence = load_time_lat_lon(csv_filepath)
  poses_sequence = preprocess_dataset(poses_sequence)

  filtered_poses_sequence = do_kalman_filtering(poses_sequence)
  print(len(filtered_poses_sequence))

  write_to_csv(csv_filepath[:-4] + '_filtered.csv', filtered_poses_sequence)
  do_graph(csv_filepath[:-4] + '_filtered.png',
           poses_sequence,
           filtered_poses_sequence)
    

if '__main__' == __name__:
  main()
